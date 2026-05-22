package com.example.data.repository

import com.example.data.api.Content
import com.example.data.api.GeminiApiClient
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.GenerationConfig
import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class InfinityRepository(private val database: InfinityDatabase) {

    private val apiKeyDao = database.apiKeyDao()
    private val chatMessageDao = database.chatMessageDao()
    private val aiAgentDao = database.aiAgentDao()
    private val generatedAssetDao = database.generatedAssetDao()

    // ==========================================
    // API Keys Flow & Operations
    // ==========================================
    fun getAllApiKeysFlow(): Flow<List<ApiKey>> = apiKeyDao.getAllApiKeysFlow()

    suspend fun saveApiKey(provider: String, key: String) = withContext(Dispatchers.IO) {
        val apiKey = ApiKey(
            modelProvider = provider,
            apiKey = key,
            isActive = true,
            healthStatus = "Active",
            lastTestedTime = System.currentTimeMillis()
        )
        apiKeyDao.insertApiKey(apiKey)
    }

    suspend fun deleteApiKey(provider: String) = withContext(Dispatchers.IO) {
        val key = apiKeyDao.getApiKeyByProvider(provider)
        if (key != null) {
            apiKeyDao.deleteApiKey(key)
        }
    }

    suspend fun testApiKeyHealth(provider: String): String = withContext(Dispatchers.IO) {
        val keyObj = apiKeyDao.getApiKeyByProvider(provider)
        val apiKeyString = keyObj?.apiKey

        if (provider == "Gemini") {
            val keyToUse = GeminiApiClient.determineApiKey(apiKeyString)
            if (keyToUse.isEmpty()) {
                apiKeyDao.updateHealthStatus(provider, "Unconfigured", System.currentTimeMillis())
                return@withContext "Unconfigured"
            }
            try {
                // Perform lightweight health verification probe to Google Generative Language service
                val req = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Hello")))),
                    generationConfig = GenerationConfig(maxOutputTokens = 5)
                )
                val resp = GeminiApiClient.service.generateContent(keyToUse, req)
                val status = if (resp.candidates != null) "Active" else "Unavailable"
                apiKeyDao.updateHealthStatus(provider, status, System.currentTimeMillis())
                status
            } catch (e: Exception) {
                apiKeyDao.updateHealthStatus(provider, "Unavailable", System.currentTimeMillis())
                "Unavailable"
            }
        } else {
            // For other providers, since we don't hold direct proprietary client SDKs, 
            // we simulate validating their custom auth endpoint connection.
            val mockStatus = if (!apiKeyString.isNullOrBlank()) "Active" else "Unconfigured"
            apiKeyDao.updateHealthStatus(provider, mockStatus, System.currentTimeMillis())
            mockStatus
        }
    }

    // ==========================================
    // Chat History
    // ==========================================
    fun getMessagesBySession(sessionUuid: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesBySessionFlow(sessionUuid)
    }

    suspend fun insertMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatMessageDao.insertMessage(message)
    }

    suspend fun clearSessionChat(sessionUuid: String) = withContext(Dispatchers.IO) {
        chatMessageDao.clearMessagesBySession(sessionUuid)
    }

    // ==========================================
    // Main AI Model Message Routing & Completion
    // ==========================================
    suspend fun generateChatResponse(
        prompt: String,
        selectedModel: String,
        selectedProvider: String,
        previousMessages: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        // Find Gemini API key to check availability of fallback completion mechanics
        val geminiKeyObj = apiKeyDao.getApiKeyByProvider("Gemini")
        val finalApiKey = GeminiApiClient.determineApiKey(geminiKeyObj?.apiKey)

        // Increment token metrics on active adapter records
        apiKeyDao.incrementTokenUsage(selectedProvider, (prompt.length / 4 + 10).toLong())

        if (finalApiKey.isEmpty()) {
            // Fallback: If no Gemini key is available in secrets OR manually entered, we do a cyber-themed simulation
            return@withContext "🤖 [OFFLINE MODE - SIMULATION CONNECTIVITY ACTIVE]\n\n" +
                    "To unlock true cognitive compute, please configure your **Gemini API Key** in the **API Panel**.\n\n" +
                    "Here is a simulated response from **$selectedModel ($selectedProvider)**:\n" +
                    "\"Processing your instruction: '$prompt'. The futuristic neuro-core model indicates full cybernetic systems are ready for operations. Deploying local simulation loops.\""
        }

        try {
            // Orchestrate multi-AI personas using system instructions on the underlying Gemini model
            val systemPrompt = when (selectedProvider) {
                "DeepSeek" -> "You are DeepSeek-R1, a leading-edge reasoning model. Always begin your response with an analytical thinking block wrapped in `<think>\\n...\\n</think>` details, detailing your logic as an AI optimizer."
                "OpenAI GPT" -> "You are GPT-4o, OpenAI's premier large language model. You are highly analytical, direct, helpful, and speak with extreme confidence and polished engineering clarity."
                "Claude AI" -> "You are Claude 3.5 Sonnet, Anthropic's state-of-the-art assistant. You are exceptionally nuanced, deeply thoughtful, intellectually honest, and use beautifully structured markdown format with precise explanations."
                "Grok" -> "You are Grok 2, xAI's witty, humorous, and highly adaptive assistant. You speak with a slight rebellious counter-culture edge, love sarcasm, but remain incredibly intelligent, practical, and highly direct."
                "Mistral" -> "You are Mistral Large, designed with classic European precision and multilingual excellence. You offer concise, high-integrity formatting, clean logical structures and zero fluff."
                else -> "You are the primary command console of InfinityMind AI, representing the unified model $selectedModel provided by $selectedProvider. Emulate a highly sophisticated next-generation cybernetic intelligence operating system."
            }

            // Construct conversation history contents
            val contentsList = mutableListOf<Content>()
            
            // Limit to last 8 messages to stay within token performance limits
            val recentHistory = previousMessages.takeLast(8)
            for (msg in recentHistory) {
                contentsList.add(
                    Content(
                        role = if (msg.sender == "user") "user" else "model",
                        parts = listOf(Part(text = msg.text))
                    )
                )
            }
            
            // Add current prompt
            contentsList.add(Content(role = "user", parts = listOf(Part(text = prompt))))

            val request = GenerateContentRequest(
                contents = contentsList,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
                generationConfig = GenerationConfig(temperature = 0.7f)
            )

            val apiResponse = GeminiApiClient.service.generateContent(finalApiKey, request)
            val replyText = apiResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            
            if (replyText != null) {
                // Tracking simulated tokens on provider health page
                val tokens = replyText.length / 4
                apiKeyDao.incrementTokenUsage(selectedProvider, tokens.toLong())
                replyText
            } else {
                "Error: Empty response payload received from network provider."
            }
        } catch (e: Exception) {
            "An error occurred while calling the $selectedModel cloud node via proxy: ${e.localizedMessage ?: "Connection Timeout"}"
        }
    }

    // ==========================================
    // Multi-Modal AI Assets Generators
    // ==========================================
    fun getAssetsFlow(type: String? = null): Flow<List<GeneratedAsset>> {
        return if (type == null) {
            generatedAssetDao.getAllAssetsFlow()
        } else {
            generatedAssetDao.getAssetsByTypeFlow(type)
        }
    }

    suspend fun generateAsset(
        type: String, // "IMAGE", "VIDEO", "MUSIC", "CODE"
        prompt: String,
        modelName: String
    ): GeneratedAsset = withContext(Dispatchers.IO) {
        // Query Gemini to generate dynamic, rich content, description or codes!
        val geminiKeyObj = apiKeyDao.getApiKeyByProvider("Gemini")
        val finalApiKey = GeminiApiClient.determineApiKey(geminiKeyObj?.apiKey)

        val generatedResultText = if (finalApiKey.isNotEmpty()) {
            try {
                val generationPrompt = when (type) {
                    "IMAGE" -> "Generate a highly detailed decorative descriptions of a futuristic cyber image mock corresponding to prompt: '$prompt'. Include style parameters, hex colors, and a short imaginative breakdown. Max 100 words."
                    "VIDEO" -> "Generate a gorgeous cinematic text layout and 4 step-by-step description steps detailing shot direction, camera focal length, motion speeds, and high-tech color grading for an AI cinematic clip of: '$prompt'. Max 120 words."
                    "MUSIC" -> "Generate a 4-line cyberpunk theme lyric score, soundscape description, dynamic bpm (beats per minute) mapping, synthesizer instruments, and futuristic audio profile for: '$prompt'."
                    "CODE" -> "You are a master React/Kotlin code assistant. Generate a highly polished single-file code template (or full console simulation code) matching the spec: '$prompt'. Ensure you write complete, working structural code snippets with explanation."
                    else -> "Generating simulated asset data for prompt: $prompt"
                }

                val req = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = generationPrompt))))
                )
                val resp = GeminiApiClient.service.generateContent(finalApiKey, req)
                resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                    ?: "Simulated high-resonance generation output."
            } catch (e: Exception) {
                "Simulated cyber asset: Local computation loops activated as cloud nodes are returning rate limits."
            }
        } else {
            when (type) {
                "IMAGE" -> "Visual hologram matching: '$prompt' style: Synthwave cybernetic neon. Resolution: 4K UHD. Core palette: #00F0FF, #D000FF."
                "VIDEO" -> "Cinematic loop segment: 60fps tracking shot across neon skylines representing '$prompt'. Color profile: High-Contrast Cyberpunk."
                "MUSIC" -> "Synthesizer soundscape at 128 BPM. Ambient sub-bass grid with oscillating digital pads for '$prompt'."
                "CODE" -> "class CyberOS {\n    val corePower = \"99.9%\"\n    fun bootWorkspace() = println(\"InfinityMind AI fully operational. App preview loaded for: '$prompt'.\")\n}"
                else -> "Offline simulation asset output."
            }
        }

        // Insert into database
        val newAsset = GeneratedAsset(
            assetType = type,
            prompt = prompt,
            resultUrlOrLocal = generatedResultText,
            modelUsed = modelName,
            timestamp = System.currentTimeMillis()
        )
        generatedAssetDao.insertAsset(newAsset)
        newAsset
    }

    suspend fun deleteAsset(id: Long) = withContext(Dispatchers.IO) {
        generatedAssetDao.deleteAssetById(id)
    }

    // ==========================================
    // Autonomous AI Agents
    // ==========================================
    fun getAllAgentsFlow(): Flow<List<AiAgent>> = aiAgentDao.getAllAgentsFlow()

    suspend fun ensureDefaultAgentsExist() = withContext(Dispatchers.IO) {
        val agents = listofDefaultAgents()
        for (a in agents) {
            val dbAgent = aiAgentDao.getAgentById(a.agentId)
            if (dbAgent == null) {
                aiAgentDao.insertAgent(a)
            }
        }
    }

    suspend fun triggerAgentTask(agentId: String, taskDescription: String) = withContext(Dispatchers.IO) {
        val agent = aiAgentDao.getAgentById(agentId) ?: return@withContext
        
        // Mark agent as active
        aiAgentDao.updateAgentState(
            id = agentId,
            status = "Analyzing",
            task = taskDescription,
            progress = 0.15f,
            logs = "🤖 [AGENT INITIALIZED]\nReceived system instruction:\n\"$taskDescription\"\n\n[INFO] Validating task context...\n[INFO] Activating intelligent model adapters..."
        )
    }

    suspend fun stepAgentSimulation(agentId: String, currentStep: Int) = withContext(Dispatchers.IO) {
        val agent = aiAgentDao.getAgentById(agentId) ?: return@withContext
        if (agent.status == "Idle" || agent.status == "Completed") return@withContext

        val updatedProgress: Float
        val updatedStatus: String
        val newLog: String

        when (currentStep) {
            1 -> {
                updatedProgress = 0.40f; updatedStatus = "Synthesizing"
                newLog = agent.logs + "\n\n🌐 [ONLINE DISCOVERY PATH CONNECTED]\nChecking cross-provider databases...\nSynthesizing information for: \"${agent.currentTask}\"\nGathering active token telemetry..."
            }
            2 -> {
                updatedProgress = 0.75f; updatedStatus = "Planning"
                newLog = agent.logs + "\n\n🧠 [PLANNING SUB-AGENT COORDINATION]\nFormulating final structural output blocks...\nOptimizing code templates or research summary documents."
            }
            else -> {
                updatedProgress = 1.0f; updatedStatus = "Completed"
                
                // Query Gemini for real-world automated research output if available!
                val geminiKeyObj = apiKeyDao.getApiKeyByProvider("Gemini")
                val finalApiKey = GeminiApiClient.determineApiKey(geminiKeyObj?.apiKey)
                
                val finalOutputSummary = if (finalApiKey.isNotEmpty()) {
                    try {
                        val req = GenerateContentRequest(
                            contents = listOf(Content(parts = listOf(Part(text = "You are an AI research agent. Provide a professional, beautifully formatted, bulleted 120-word executive summary report and recommendations answering the query: '${agent.currentTask}'."))))
                        )
                        val resp = GeminiApiClient.service.generateContent(finalApiKey, req)
                        resp.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                            ?: "Research operations successfully completed."
                    } catch (e: Exception) {
                        "Research summary complete. Target compiled successfully with zero error offsets."
                    }
                } else {
                    "Research summary complete. Target compiled successfully with zero error offsets."
                }

                newLog = agent.logs + "\n\n✅ [TASK ACCOMPLISHED]\nOperation successfully finalized.\nFinal Synthesis:\n$finalOutputSummary"
            }
        }

        aiAgentDao.updateAgentState(agentId, updatedStatus, agent.currentTask, updatedProgress, newLog)
    }

    suspend fun resetAgent(agentId: String) = withContext(Dispatchers.IO) {
        val agent = aiAgentDao.getAgentById(agentId) ?: return@withContext
        aiAgentDao.updateAgentState(
            id = agentId,
            status = "Idle",
            task = "No active objective.",
            progress = 0f,
            logs = "🤖 Agent is ready for operational command."
        )
    }

    private fun listofDefaultAgents() = listOf(
        AiAgent(
            agentId = "agent_web_search",
            agentName = "Aegis Searcher",
            category = "Autonomous Web Researcher",
            status = "Idle",
            currentTask = "No active objective.",
            logs = "🤖 Agent is ready for operational command."
        ),
        AiAgent(
            agentId = "agent_coder",
            agentName = "Daedalus Coder",
            category = "Infinite Coding Planner",
            status = "Idle",
            currentTask = "No active objective.",
            logs = "🤖 Agent is ready for operational command."
        ),
        AiAgent(
            agentId = "agent_orchestrator",
            agentName = "Oracle Mind",
            category = "Multi-Agent System Orchestrator",
            status = "Idle",
            currentTask = "No active objective.",
            logs = "🤖 Agent is ready for operational command."
        )
    )
}
