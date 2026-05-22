package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.InfinityRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class InfinityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: InfinityRepository

    init {
        val database = InfinityDatabase.getDatabase(application)
        repository = InfinityRepository(database)
        
        // Initialize default autonomous agents in database
        viewModelScope.launch {
            repository.ensureDefaultAgentsExist()
            // Provide startup preset keys so fields display nicely
            ensurePresetApiKeys()
        }
    }

    // ==========================================
    // Core Navigation & Window State
    // ==========================================
    private val _activeTab = MutableStateFlow("CHATS") // "CHATS", "GENERATORS", "AGENTS", "APIS", "STORE"
    val activeTab: StateFlow<String> = _activeTab.asStateFlow()

    fun setActiveTab(tab: String) {
        _activeTab.value = tab
    }

    // ==========================================
    // API Keys Dashboard
    // ==========================================
    val apiKeysList: StateFlow<List<ApiKey>> = repository.getAllApiKeysFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _apiTestingStatus = MutableStateFlow<Map<String, String>>(emptyMap())
    val apiTestingStatus: StateFlow<Map<String, String>> = _apiTestingStatus.asStateFlow()

    fun updateApiKey(provider: String, key: String) {
        viewModelScope.launch {
            repository.saveApiKey(provider, key)
            testApiKeyHealth(provider)
        }
    }

    fun deleteApiKey(provider: String) {
        viewModelScope.launch {
            repository.deleteApiKey(provider)
            _apiTestingStatus.update { it + (provider to "Unconfigured") }
        }
    }

    fun testApiKeyHealth(provider: String) {
        viewModelScope.launch {
            _apiTestingStatus.update { it + (provider to "Testing...") }
            val health = repository.testApiKeyHealth(provider)
            _apiTestingStatus.update { it + (provider to health) }
        }
    }

    fun testAllApiKeys() {
        viewModelScope.launch {
            val providers = listOf(
                "OpenAI GPT", "Claude AI", "Gemini", "Mistral", 
                "DeepSeek", "Grok", "Stability AI", "ElevenLabs"
            )
            for (p in providers) {
                testApiKeyHealth(p)
            }
        }
    }

    private suspend fun ensurePresetApiKeys() {
        val currentKeys = repository.getAllApiKeysFlow().firstOrNull() ?: emptyList()
        val providers = listOf(
            "OpenAI GPT", "Claude AI", "Gemini", "Mistral", 
            "DeepSeek", "Grok", "Stability AI", "ElevenLabs", "Runway", "Replicate"
        )
        for (p in providers) {
            if (currentKeys.none { it.modelProvider == p }) {
                // Initialize placeholder so they populate on the dashboard
                repository.saveApiKey(p, "")
                _apiTestingStatus.update { it + (p to "Unconfigured") }
            }
        }
    }

    // ==========================================
    // Multi-AI Chat System
    // ==========================================
    private val _sessionUuid = MutableStateFlow(UUID.randomUUID().toString())
    val sessionUuid: StateFlow<String> = _sessionUuid.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isChatLoadingLeft = MutableStateFlow(false)
    val isChatLoadingLeft: StateFlow<Boolean> = _isChatLoadingLeft.asStateFlow()

    private val _isChatLoadingRight = MutableStateFlow(false)
    val isChatLoadingRight: StateFlow<Boolean> = _isChatLoadingRight.asStateFlow()

    // Model configurations
    private val _selectedModelLeft = MutableStateFlow("Gemini 3.5 Flash")
    val selectedModelLeft: StateFlow<String> = _selectedModelLeft.asStateFlow()

    private val _selectedModelRight = MutableStateFlow("DeepSeek-R1")
    val selectedModelRight: StateFlow<String> = _selectedModelRight.asStateFlow()

    private val _comparisonMode = MutableStateFlow(false)
    val comparisonMode: StateFlow<Boolean> = _comparisonMode.asStateFlow()

    // Message lists reactively bound to database session flows
    val chatMessagesLeft: StateFlow<List<ChatMessage>> = _sessionUuid
        .flatMapLatest { uuid -> repository.getMessagesBySession("${uuid}_left") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessagesRight: StateFlow<List<ChatMessage>> = _sessionUuid
        .flatMapLatest { uuid -> repository.getMessagesBySession("${uuid}_right") }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Model selection rules mapping model name to platform provider
    fun getProviderForModel(model: String): String {
        return when {
            model.contains("GPT") || model.contains("OpenAI") -> "OpenAI GPT"
            model.contains("Claude") -> "Claude AI"
            model.contains("Gemini") -> "Gemini"
            model.contains("DeepSeek") -> "DeepSeek"
            model.contains("Grok") -> "Grok"
            model.contains("Mistral") -> "Mistral"
            else -> "Gemini"
        }
    }

    fun setModelLeft(model: String) {
        _selectedModelLeft.value = model
    }

    fun setModelRight(model: String) {
        _selectedModelRight.value = model
    }

    fun setComparisonMode(enabled: Boolean) {
        _comparisonMode.value = enabled
    }

    fun setChatInput(input: String) {
        _chatInput.value = input
    }

    fun createNewChatSession() {
        _sessionUuid.value = UUID.randomUUID().toString()
    }

    fun sendChatMessage() {
        val input = _chatInput.value.trim()
        if (input.isEmpty()) return

        _chatInput.value = ""
        val currentSession = _sessionUuid.value

        viewModelScope.launch {
            // Left message processing
            val primaryModel = _selectedModelLeft.value
            val primaryProvider = getProviderForModel(primaryModel)
            
            val userMsgLeft = ChatMessage(
                sessionUuid = "${currentSession}_left",
                sender = "user",
                text = input,
                modelName = primaryModel,
                provider = primaryProvider
            )
            repository.insertMessage(userMsgLeft)
            
            _isChatLoadingLeft.value = true
            
            // Get current message thread for background contextual flow
            val leftHistory = chatMessagesLeft.value
            
            launch {
                val responseText = repository.generateChatResponse(
                    prompt = input,
                    selectedModel = primaryModel,
                    selectedProvider = primaryProvider,
                    previousMessages = leftHistory
                )
                
                val aiMsgLeft = ChatMessage(
                    sessionUuid = "${currentSession}_left",
                    sender = "ai",
                    text = responseText,
                    modelName = primaryModel,
                    provider = primaryProvider
                )
                repository.insertMessage(aiMsgLeft)
                _isChatLoadingLeft.value = false
            }

            // Right message processing (Only triggered if side-by-side comparison mode is active!)
            if (_comparisonMode.value) {
                val secondaryModel = _selectedModelRight.value
                val secondaryProvider = getProviderForModel(secondaryModel)

                val userMsgRight = ChatMessage(
                    sessionUuid = "${currentSession}_right",
                    sender = "user",
                    text = input,
                    modelName = secondaryModel,
                    provider = secondaryProvider
                )
                repository.insertMessage(userMsgRight)

                _isChatLoadingRight.value = true
                val rightHistory = chatMessagesRight.value

                launch {
                    val responseTextRight = repository.generateChatResponse(
                        prompt = input,
                        selectedModel = secondaryModel,
                        selectedProvider = secondaryProvider,
                        previousMessages = rightHistory
                    )

                    val aiMsgRight = ChatMessage(
                        sessionUuid = "${currentSession}_right",
                        sender = "ai",
                        text = responseTextRight,
                        modelName = secondaryModel,
                        provider = secondaryProvider
                    )
                    repository.insertMessage(aiMsgRight)
                    _isChatLoadingRight.value = false
                }
            }
        }
    }

    fun clearActiveChats() {
        val currentSession = _sessionUuid.value
        viewModelScope.launch {
            repository.clearSessionChat("${currentSession}_left")
            repository.clearSessionChat("${currentSession}_right")
        }
    }

    // ==========================================
    // AI Asset Generators
    // ==========================================
    private val _generatorPrompt = MutableStateFlow("")
    val generatorPrompt: StateFlow<String> = _generatorPrompt.asStateFlow()

    private val _generatorType = MutableStateFlow("IMAGE") // "IMAGE", "VIDEO", "MUSIC", "CODE"
    val generatorType: StateFlow<String> = _generatorType.asStateFlow()

    private val _generatorModel = MutableStateFlow("Stability AI - Flux.1")
    val generatorModel: StateFlow<String> = _generatorModel.asStateFlow()

    private val _isGeneratingAsset = MutableStateFlow(false)
    val isGeneratingAsset: StateFlow<Boolean> = _isGeneratingAsset.asStateFlow()

    private val _activeGeneratedAsset = MutableStateFlow<GeneratedAsset?>(null)
    val activeGeneratedAsset: StateFlow<GeneratedAsset?> = _activeGeneratedAsset.asStateFlow()

    val generatedAssetsList: StateFlow<List<GeneratedAsset>> = repository.getAssetsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setGeneratorPrompt(prompt: String) {
        _generatorPrompt.value = prompt
    }

    fun setGeneratorType(type: String) {
        _generatorType.value = type
        // Reset associated preview models
        _generatorModel.value = when (type) {
            "IMAGE" -> "Stability AI - Flux.1"
            "VIDEO" -> "Runway Gen-3 Alpha"
            "MUSIC" -> "Suno AI v4"
            "CODE" -> "InfinityMind - Coder v2"
            else -> "Stability AI - Flux.1"
        }
    }

    fun setGeneratorModel(model: String) {
        _generatorModel.value = model
    }

    fun generateAsset() {
        val prompt = _generatorPrompt.value.trim()
        if (prompt.isEmpty()) return

        _isGeneratingAsset.value = true
        _generatorPrompt.value = ""

        viewModelScope.launch {
            val asset = repository.generateAsset(
                type = _generatorType.value,
                prompt = prompt,
                modelName = _generatorModel.value
            )
            _activeGeneratedAsset.value = asset
            _isGeneratingAsset.value = false
        }
    }

    fun deleteAsset(assetId: Long) {
        viewModelScope.launch {
            repository.deleteAsset(assetId)
            if (_activeGeneratedAsset.value?.id == assetId) {
                _activeGeneratedAsset.value = null
            }
        }
    }

    // ==========================================
    // Autonomous AI Agents
    // ==========================================
    val aiAgentsList: StateFlow<List<AiAgent>> = repository.getAllAgentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedAgentId = MutableStateFlow<String?>("agent_web_search")
    val selectedAgentId: StateFlow<String?> = _selectedAgentId.asStateFlow()

    private val _agentInputTask = MutableStateFlow("")
    val agentInputTask: StateFlow<String> = _agentInputTask.asStateFlow()

    fun selectAgent(agentId: String) {
        _selectedAgentId.value = agentId
    }

    fun setAgentInputTask(task: String) {
        _agentInputTask.value = task
    }

    fun triggerAgentAutomation() {
        val task = _agentInputTask.value.trim()
        val agentId = _selectedAgentId.value
        if (task.isEmpty() || agentId == null) return

        _agentInputTask.value = ""

        viewModelScope.launch {
            repository.triggerAgentTask(agentId, task)
            
            // Run a background operation cycle simulating active sub-steps in intervals!
            launch {
                delay(2000)
                repository.stepAgentSimulation(agentId, 1) // Step 1
                delay(3000)
                repository.stepAgentSimulation(agentId, 2) // Step 2
                delay(3000)
                repository.stepAgentSimulation(agentId, 3) // Step 3 Complete
            }
        }
    }

    fun resetAgentState(agentId: String) {
        viewModelScope.launch {
            repository.resetAgent(agentId)
        }
    }
}
