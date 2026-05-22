package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// ==========================================
// Entities
// ==========================================

@Entity(tableName = "api_keys")
data class ApiKey(
    @PrimaryKey val modelProvider: String, // e.g. "OpenAI GPT", "Claude AI", "Gemini", "Mistral", "DeepSeek", "Grok", "Stability AI"
    val apiKey: String,
    val isActive: Boolean = true,
    val usageTokens: Long = 0,
    val healthStatus: String = "Active", // "Active", "Unavailable", "Unconfigured"
    val lastTestedTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionUuid: String,
    val sender: String, // "user", "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelName: String, // "GPT-4o", "Claude 3.5", "Gemini 3.5 Flash", etc.
    val provider: String // "OpenAI GPT", "Claude AI", "Gemini", etc.
)

@Entity(tableName = "ai_agents")
data class AiAgent(
    @PrimaryKey val agentId: String,
    val agentName: String,
    val category: String, // "Autonomous Web Researcher", "Infinite Coding Planner", "Multi-Agent System Orchestrator"
    val status: String, // "Idle", "Analyzing", "Synthesizing", "Completed"
    val currentTask: String,
    val progress: Float = 0f,
    val logs: String = "" // Newline separated logger output
)

@Entity(tableName = "generated_assets")
data class GeneratedAsset(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetType: String, // "IMAGE", "VIDEO", "MUSIC", "CODE"
    val prompt: String,
    val resultUrlOrLocal: String,
    val modelUsed: String,
    val timestamp: Long = System.currentTimeMillis(),
    val detailsJson: String = ""
)

// ==========================================
// DAOs
// ==========================================

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys")
    fun getAllApiKeysFlow(): Flow<List<ApiKey>>

    @Query("SELECT * FROM api_keys")
    suspend fun getAllApiKeys(): List<ApiKey>

    @Query("SELECT * FROM api_keys WHERE modelProvider = :provider LIMIT 1")
    suspend fun getApiKeyByProvider(provider: String): ApiKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(key: ApiKey)

    @Delete
    suspend fun deleteApiKey(key: ApiKey)

    @Query("UPDATE api_keys SET healthStatus = :status, lastTestedTime = :time WHERE modelProvider = :provider")
    suspend fun updateHealthStatus(provider: String, status: String, time: Long)

    @Query("UPDATE api_keys SET usageTokens = usageTokens + :addCount WHERE modelProvider = :provider")
    suspend fun incrementTokenUsage(provider: String, addCount: Long)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessage>>

    @Query("SELECT * FROM chat_messages WHERE sessionUuid = :sessionUuid ORDER BY timestamp ASC")
    fun getMessagesBySessionFlow(sessionUuid: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()

    @Query("DELETE FROM chat_messages WHERE sessionUuid = :sessionUuid")
    suspend fun clearMessagesBySession(sessionUuid: String)
}

@Dao
interface AiAgentDao {
    @Query("SELECT * FROM ai_agents")
    fun getAllAgentsFlow(): Flow<List<AiAgent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AiAgent)

    @Query("UPDATE ai_agents SET status = :status, currentTask = :task, progress = :progress, logs = :logs WHERE agentId = :id")
    suspend fun updateAgentState(id: String, status: String, task: String, progress: Float, logs: String)

    @Query("SELECT * FROM ai_agents WHERE agentId = :id LIMIT 1")
    suspend fun getAgentById(id: String): AiAgent?
}

@Dao
interface GeneratedAssetDao {
    @Query("SELECT * FROM generated_assets ORDER BY timestamp DESC")
    fun getAllAssetsFlow(): Flow<List<GeneratedAsset>>

    @Query("SELECT * FROM generated_assets WHERE assetType = :type ORDER BY timestamp DESC")
    fun getAssetsByTypeFlow(type: String): Flow<List<GeneratedAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: GeneratedAsset)

    @Query("DELETE FROM generated_assets WHERE id = :id")
    suspend fun deleteAssetById(id: Long)
}

// ==========================================
// Database
// ==========================================

@Database(
    entities = [ApiKey::class, ChatMessage::class, AiAgent::class, GeneratedAsset::class],
    version = 1,
    exportSchema = false
)
abstract class InfinityDatabase : RoomDatabase() {
    abstract fun apiKeyDao(): ApiKeyDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun aiAgentDao(): AiAgentDao
    abstract fun generatedAssetDao(): GeneratedAssetDao

    companion object {
        @Volatile
        private var INSTANCE: InfinityDatabase? = null

        fun getDatabase(context: android.content.Context): InfinityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InfinityDatabase::class.java,
                    "infinity_mind_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
