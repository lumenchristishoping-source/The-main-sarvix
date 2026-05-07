package com.sarvix.app.data.repository

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.sarvix.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class TranslationRepository @Inject constructor() {
    
    private val translatorCache = mutableMapOf<String, com.google.mlkit.nl.translate.Translator>()
    
    suspend fun translateText(text: String, targetLanguageCode: String): String {
        if (text.isBlank()) return ""
        
        return try {
            val sourceLanguage = detectLanguage(text)
            
            // Don't translate if already in target language
            if (sourceLanguage == targetLanguageCode) {
                return text
            }
            
            val translator = getOrCreateTranslator(sourceLanguage, targetLanguageCode)
            
            suspendCancellableCoroutine { continuation ->
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        continuation.resume(translatedText)
                    }
                    .addOnFailureListener { exception ->
                        Timber.e(exception, "Translation failed")
                        continuation.resumeWithException(exception)
                    }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in translation")
            text // Return original text on error
        }
    }

    fun translateTextFlow(text: String, targetLanguageCode: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val translated = translateText(text, targetLanguageCode)
            emit(Resource.Success(translated))
        } catch (e: Exception) {
            Timber.e(e, "Translation error")
            emit(Resource.Error(e.message ?: "Translation failed"))
        }
    }

    private fun detectLanguage(text: String): String {
        // For MVP, we'll use English as default source
        // In production, use ML Kit's language identification
        return TranslateLanguage.ENGLISH
    }

    private fun getOrCreateTranslator(sourceLanguage: String, targetLanguage: String): com.google.mlkit.nl.translate.Translator {
        val cacheKey = "$sourceLanguage-$targetLanguage"
        
        return translatorCache.getOrPut(cacheKey) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLanguage)
                .setTargetLanguage(targetLanguage)
                .build()
            
            Translation.getClient(options)
        }
    }

    fun downloadLanguageModel(languageCode: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(languageCode)
                .build()
            
            val translator = Translation.getClient(options)
            
            suspendCancellableCoroutine { continuation ->
                translator.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        continuation.resume(true)
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            }
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error downloading language model")
            emit(Resource.Error(e.message ?: "Failed to download language model"))
        }
    }

    fun getSupportedLanguages(): List<Pair<String, String>> {
        return TranslateLanguage.getAllLanguages().map { code ->
            code to getLanguageName(code)
        }.sortedBy { it.second }
    }

    private fun getLanguageName(code: String): String {
        return when (code) {
            TranslateLanguage.ENGLISH -> "English"
            TranslateLanguage.SPANISH -> "Spanish"
            TranslateLanguage.FRENCH -> "French"
            TranslateLanguage.GERMAN -> "German"
            TranslateLanguage.ITALIAN -> "Italian"
            TranslateLanguage.PORTUGUESE -> "Portuguese"
            TranslateLanguage.RUSSIAN -> "Russian"
            TranslateLanguage.JAPANESE -> "Japanese"
            TranslateLanguage.KOREAN -> "Korean"
            TranslateLanguage.CHINESE -> "Chinese"
            TranslateLanguage.ARABIC -> "Arabic"
            TranslateLanguage.HINDI -> "Hindi"
            TranslateLanguage.BENGALI -> "Bengali"
            "pa" -> "Punjabi"
            TranslateLanguage.TAMIL -> "Tamil"
            TranslateLanguage.TELUGU -> "Telugu"
            TranslateLanguage.MARATHI -> "Marathi"
            TranslateLanguage.URDU -> "Urdu"
            TranslateLanguage.TURKISH -> "Turkish"
            TranslateLanguage.VIETNAMESE -> "Vietnamese"
            TranslateLanguage.THAI -> "Thai"
            TranslateLanguage.INDONESIAN -> "Indonesian"
            TranslateLanguage.MALAY -> "Malay"
            TranslateLanguage.POLISH -> "Polish"
            TranslateLanguage.UKRAINIAN -> "Ukrainian"
            TranslateLanguage.ROMANIAN -> "Romanian"
            TranslateLanguage.DUTCH -> "Dutch"
            TranslateLanguage.GREEK -> "Greek"
            TranslateLanguage.CZECH -> "Czech"
            TranslateLanguage.HUNGARIAN -> "Hungarian"
            TranslateLanguage.SWEDISH -> "Swedish"
            TranslateLanguage.DANISH -> "Danish"
            TranslateLanguage.FINNISH -> "Finnish"
            TranslateLanguage.NORWEGIAN -> "Norwegian"
            TranslateLanguage.HEBREW -> "Hebrew"
            TranslateLanguage.PERSIAN -> "Persian"
            TranslateLanguage.SWAHILI -> "Swahili"
            TranslateLanguage.AFRIKAANS -> "Afrikaans"
            TranslateLanguage.ALBANIAN -> "Albanian"
            "hy" -> "Armenian"
            "az" -> "Azerbaijani"
            "eu" -> "Basque"
            TranslateLanguage.BELARUSIAN -> "Belarusian"
            TranslateLanguage.BULGARIAN -> "Bulgarian"
            TranslateLanguage.CATALAN -> "Catalan"
            TranslateLanguage.CROATIAN -> "Croatian"
            TranslateLanguage.ESTONIAN -> "Estonian"
            TranslateLanguage.GALICIAN -> "Galician"
            TranslateLanguage.GEORGIAN -> "Georgian"
            TranslateLanguage.GUJARATI -> "Gujarati"
            TranslateLanguage.HAITIAN_CREOLE -> "Haitian Creole"
            "ha" -> "Hausa"
            TranslateLanguage.ICELANDIC -> "Icelandic"
            "ig" -> "Igbo"
            TranslateLanguage.IRISH -> "Irish"
            "jv" -> "Javanese"
            TranslateLanguage.KANNADA -> "Kannada"
            "kk" -> "Kazakh"
            "km" -> "Khmer"
            "ky" -> "Kyrgyz"
            "lo" -> "Lao"
            TranslateLanguage.LATVIAN -> "Latvian"
            TranslateLanguage.LITHUANIAN -> "Lithuanian"
            "lb" -> "Luxembourgish"
            TranslateLanguage.MACEDONIAN -> "Macedonian"
            "mg" -> "Malagasy"
            "ml" -> "Malayalam"
            TranslateLanguage.MALTESE -> "Maltese"
            "mi" -> "Maori"
            "mn" -> "Mongolian"
            "ne" -> "Nepali"
            "ps" -> "Pashto"
            "sr" -> "Serbian"
            "si" -> "Sinhala"
            TranslateLanguage.SLOVAK -> "Slovak"
            TranslateLanguage.SLOVENIAN -> "Slovenian"
            "so" -> "Somali"
            "su" -> "Sundanese"
            TranslateLanguage.TAGALOG -> "Tagalog"
            "tg" -> "Tajik"
            "tt" -> "Tatar"
            "bo" -> "Tibetan"
            "tk" -> "Turkmen"
            "ug" -> "Uyghur"
            "uz" -> "Uzbek"
            TranslateLanguage.WELSH -> "Welsh"
            "fy" -> "Western Frisian"
            "xh" -> "Xhosa"
            "yi" -> "Yiddish"
            "yo" -> "Yoruba"
            "zu" -> "Zulu"
            else -> code
        }
    }
}