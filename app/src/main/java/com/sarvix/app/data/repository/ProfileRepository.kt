package com.sarvix.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.data.model.User
import com.sarvix.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    fun getUserProfile(userId: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val user = doc.toObject(User::class.java)
            if (user != null) {
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("User not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting user profile")
            emit(Resource.Error(e.message ?: "Failed to load profile"))
        }
    }

    fun updateProfile(
        userId: String,
        displayName: String? = null,
        bio: String? = null,
        mood: MoodStatus? = null,
        interests: List<String>? = null,
        country: String? = null,
        countryCode: String? = null,
        language: String? = null,
        languageCode: String? = null
    ): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val updates = mutableMapOf<String, Any>()
            displayName?.let { updates["displayName"] = it }
            bio?.let { updates["bio"] = it }
            mood?.let { updates["mood"] = it.name }
            interests?.let { updates["interests"] = it }
            country?.let { updates["country"] = it }
            countryCode?.let { updates["countryCode"] = it }
            language?.let { updates["language"] = it }
            languageCode?.let { updates["languageCode"] = it }
            
            // Check if profile is complete
            val currentDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val currentUser = currentDoc.toObject(User::class.java)
            val finalInterests = interests ?: currentUser?.interests ?: emptyList()
            val finalCountry = country ?: currentUser?.country ?: ""
            val finalLanguage = language ?: currentUser?.language ?: ""
            
            val isComplete = finalInterests.size >= 3 && 
                           finalCountry.isNotBlank() && 
                           finalLanguage.isNotBlank()
            
            updates["isProfileComplete"] = isComplete
            
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error updating profile")
            emit(Resource.Error(e.message ?: "Failed to update profile"))
        }
    }

    fun uploadProfilePicture(userId: String, imageUri: Uri): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        try {
            val storageRef = storage.reference
                .child("profile_pictures")
                .child("$userId.jpg")
            
            storageRef.putFile(imageUri).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            
            // Update user document with new photo URL
            firestore.collection("users")
                .document(userId)
                .update("profilePictureUrl", downloadUrl)
                .await()
            
            emit(Resource.Success(downloadUrl))
        } catch (e: Exception) {
            Timber.e(e, "Error uploading profile picture")
            emit(Resource.Error(e.message ?: "Failed to upload image"))
        }
    }

    fun updateMood(userId: String, mood: MoodStatus): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            firestore.collection("users")
                .document(userId)
                .update("mood", mood.name)
                .await()
            emit(Resource.Success(true))
        } catch (e: Exception) {
            Timber.e(e, "Error updating mood")
            emit(Resource.Error(e.message ?: "Failed to update mood"))
        }
    }

    fun searchUsersByUsername(query: String): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        try {
            // Search for usernames starting with @
            val searchQuery = if (query.startsWith("@")) query else "@$query"
            
            val snapshot = firestore.collection("users")
                .orderBy("username")
                .startAt(searchQuery)
                .endAt(searchQuery + "\uf8ff")
                .limit(20)
                .get()
                .await()
            
            val users = snapshot.toObjects(User::class.java)
            emit(Resource.Success(users))
        } catch (e: Exception) {
            Timber.e(e, "Error searching users")
            emit(Resource.Error(e.message ?: "Failed to search users"))
        }
    }

    // Predefined interests list
    fun getAvailableInterests(): List<String> = listOf(
        "Technology", "Music", "Sports", "Art", "Travel", "Food", "Photography",
        "Gaming", "Reading", "Movies", "Fitness", "Fashion", "Science", "History",
        "Nature", "Politics", "Business", "Education", "Health", "Cooking",
        "Writing", "Dancing", "Hiking", "Yoga", "Meditation", "Programming",
        "Design", "Entrepreneurship", "Philosophy", "Psychology", "Languages",
        "Culture", "Animals", "Environment", "Space", "Finance", "DIY",
        "Gardening", "Crafts", "Collecting", "Volunteering", "Social Justice"
    )

    fun getAvailableCountries(): List<Pair<String, String>> = listOf(
        "US" to "United States",
        "GB" to "United Kingdom",
        "CA" to "Canada",
        "AU" to "Australia",
        "DE" to "Germany",
        "FR" to "France",
        "ES" to "Spain",
        "IT" to "Italy",
        "JP" to "Japan",
        "KR" to "South Korea",
        "CN" to "China",
        "IN" to "India",
        "BR" to "Brazil",
        "MX" to "Mexico",
        "RU" to "Russia",
        "ZA" to "South Africa",
        "NG" to "Nigeria",
        "EG" to "Egypt",
        "TR" to "Turkey",
        "SA" to "Saudi Arabia",
        "AE" to "United Arab Emirates",
        "SG" to "Singapore",
        "TH" to "Thailand",
        "VN" to "Vietnam",
        "ID" to "Indonesia",
        "PH" to "Philippines",
        "MY" to "Malaysia",
        "NL" to "Netherlands",
        "SE" to "Sweden",
        "NO" to "Norway",
        "DK" to "Denmark",
        "FI" to "Finland",
        "PL" to "Poland",
        "UA" to "Ukraine",
        "AR" to "Argentina",
        "CL" to "Chile",
        "CO" to "Colombia",
        "PE" to "Peru",
        "KE" to "Kenya",
        "GH" to "Ghana",
        "PK" to "Pakistan",
        "BD" to "Bangladesh",
        "LK" to "Sri Lanka",
        "NP" to "Nepal",
        "NZ" to "New Zealand",
        "IE" to "Ireland",
        "PT" to "Portugal",
        "GR" to "Greece",
        "CZ" to "Czech Republic",
        "HU" to "Hungary",
        "RO" to "Romania",
        "BG" to "Bulgaria",
        "HR" to "Croatia",
        "RS" to "Serbia",
        "IL" to "Israel",
        "IR" to "Iran",
        "IQ" to "Iraq",
        "SY" to "Syria",
        "JO" to "Jordan",
        "LB" to "Lebanon",
        "KW" to "Kuwait",
        "QA" to "Qatar",
        "BH" to "Bahrain",
        "OM" to "Oman",
        "YE" to "Yemen",
        "AF" to "Afghanistan",
        "UZ" to "Uzbekistan",
        "KZ" to "Kazakhstan",
        "AZ" to "Azerbaijan",
        "GE" to "Georgia",
        "AM" to "Armenia",
        "MN" to "Mongolia",
        "KP" to "North Korea",
        "TW" to "Taiwan",
        "HK" to "Hong Kong",
        "MO" to "Macau",
        "MM" to "Myanmar",
        "KH" to "Cambodia",
        "LA" to "Laos",
        "BN" to "Brunei",
        "TL" to "Timor-Leste",
        "PG" to "Papua New Guinea",
        "FJ" to "Fiji",
        "SB" to "Solomon Islands",
        "VU" to "Vanuatu",
        "NC" to "New Caledonia",
        "PF" to "French Polynesia",
        "GU" to "Guam",
        "AS" to "American Samoa",
        "WS" to "Samoa",
        "TO" to "Tonga",
        "KI" to "Kiribati",
        "TV" to "Tuvalu",
        "NR" to "Nauru",
        "PW" to "Palau",
        "MH" to "Marshall Islands",
        "FM" to "Micronesia",
        "IS" to "Iceland",
        "MT" to "Malta",
        "CY" to "Cyprus",
        "LU" to "Luxembourg",
        "BE" to "Belgium",
        "CH" to "Switzerland",
        "AT" to "Austria",
        "SK" to "Slovakia",
        "SI" to "Slovenia",
        "LT" to "Lithuania",
        "LV" to "Latvia",
        "EE" to "Estonia",
        "MD" to "Moldova",
        "BY" to "Belarus",
        "AL" to "Albania",
        "BA" to "Bosnia and Herzegovina",
        "ME" to "Montenegro",
        "MK" to "North Macedonia",
        "XK" to "Kosovo",
        "AD" to "Andorra",
        "MC" to "Monaco",
        "LI" to "Liechtenstein",
        "SM" to "San Marino",
        "VA" to "Vatican City",
        "GI" to "Gibraltar",
        "FO" to "Faroe Islands",
        "AX" to "Åland Islands",
        "SJ" to "Svalbard and Jan Mayen",
        "GL" to "Greenland",
        "BM" to "Bermuda",
        "KY" to "Cayman Islands",
        "BS" to "Bahamas",
        "JM" to "Jamaica",
        "HT" to "Haiti",
        "DO" to "Dominican Republic",
        "CU" to "Cuba",
        "PR" to "Puerto Rico",
        "VI" to "U.S. Virgin Islands",
        "VG" to "British Virgin Islands",
        "AI" to "Anguilla",
        "MS" to "Montserrat",
        "KN" to "Saint Kitts and Nevis",
        "AG" to "Antigua and Barbuda",
        "DM" to "Dominica",
        "LC" to "Saint Lucia",
        "VC" to "Saint Vincent and the Grenadines",
        "GD" to "Grenada",
        "BB" to "Barbados",
        "TT" to "Trinidad and Tobago",
        "GY" to "Guyana",
        "SR" to "Suriname",
        "GF" to "French Guiana",
        "EC" to "Ecuador",
        "BO" to "Bolivia",
        "PY" to "Paraguay",
        "UY" to "Uruguay",
        "FK" to "Falkland Islands",
        "GS" to "South Georgia and the South Sandwich Islands",
        "AQ" to "Antarctica",
        "CV" to "Cape Verde",
        "GM" to "Gambia",
        "SN" to "Senegal",
        "MR" to "Mauritania",
        "ML" to "Mali",
        "GN" to "Guinea",
        "SL" to "Sierra Leone",
        "LR" to "Liberia",
        "CI" to "Ivory Coast",
        "BF" to "Burkina Faso",
        "NE" to "Niger",
        "TG" to "Togo",
        "BJ" to "Benin",
        "GW" to "Guinea-Bissau",
        "GW" to "Guinea-Bissau",
        "ST" to "São Tomé and Príncipe",
        "GQ" to "Equatorial Guinea",
        "GA" to "Gabon",
        "CG" to "Republic of the Congo",
        "CD" to "Democratic Republic of the Congo",
        "UG" to "Uganda",
        "RW" to "Rwanda",
        "BI" to "Burundi",
        "TZ" to "Tanzania",
        "MW" to "Malawi",
        "ZM" to "Zambia",
        "ZW" to "Zimbabwe",
        "MZ" to "Mozambique",
        "MG" to "Madagascar",
        "MU" to "Mauritius",
        "SC" to "Seychelles",
        "KM" to "Comoros",
        "DJ" to "Djibouti",
        "ER" to "Eritrea",
        "ET" to "Ethiopia",
        "SO" to "Somalia",
        "SD" to "Sudan",
        "SS" to "South Sudan",
        "CF" to "Central African Republic",
        "TD" to "Chad",
        "CM" to "Cameroon",
        "GQ" to "Equatorial Guinea",
        "GA" to "Gabon",
        "CG" to "Republic of the Congo",
        "CD" to "Democratic Republic of the Congo",
        "AO" to "Angola",
        "NA" to "Namibia",
        "BW" to "Botswana",
        "SZ" to "Eswatini",
        "LS" to "Lesotho",
        "RE" to "Réunion",
        "YT" to "Mayotte",
        "SH" to "Saint Helena, Ascension and Tristan da Cunha",
        "EH" to "Western Sahara",
        "MA" to "Morocco",
        "DZ" to "Algeria",
        "TN" to "Tunisia",
        "LY" to "Libya",
        "EG" to "Egypt",
        "IL" to "Israel",
        "PS" to "Palestine",
        "LB" to "Lebanon",
        "SY" to "Syria",
        "TR" to "Turkey",
        "CY" to "Cyprus",
        "GE" to "Georgia",
        "AM" to "Armenia",
        "AZ" to "Azerbaijan",
        "IR" to "Iran",
        "IQ" to "Iraq",
        "KW" to "Kuwait",
        "SA" to "Saudi Arabia",
        "BH" to "Bahrain",
        "QA" to "Qatar",
        "AE" to "United Arab Emirates",
        "OM" to "Oman",
        "YE" to "Yemen",
        "MN" to "Mongolia",
        "CN" to "China",
        "KP" to "North Korea",
        "KR" to "South Korea",
        "JP" to "Japan",
        "IN" to "India",
        "PK" to "Pakistan",
        "BD" to "Bangladesh",
        "LK" to "Sri Lanka",
        "MV" to "Maldives",
        "NP" to "Nepal",
        "BT" to "Bhutan",
        "MM" to "Myanmar",
        "TH" to "Thailand",
        "LA" to "Laos",
        "KH" to "Cambodia",
        "VN" to "Vietnam",
        "MY" to "Malaysia",
        "SG" to "Singapore",
        "ID" to "Indonesia",
        "BN" to "Brunei",
        "PH" to "Philippines",
        "TW" to "Taiwan",
        "HK" to "Hong Kong",
        "MO" to "Macau",
        "TL" to "East Timor",
        "AU" to "Australia",
        "NZ" to "New Zealand",
        "PG" to "Papua New Guinea",
        "FJ" to "Fiji",
        "SB" to "Solomon Islands",
        "VU" to "Vanuatu",
        "NC" to "New Caledonia",
        "PF" to "French Polynesia",
        "GU" to "Guam",
        "AS" to "American Samoa",
        "WS" to "Samoa",
        "TO" to "Tonga",
        "KI" to "Kiribati",
        "TV" to "Tuvalu",
        "NR" to "Nauru",
        "PW" to "Palau",
        "MH" to "Marshall Islands",
        "FM" to "Micronesia",
        "US" to "United States",
        "CA" to "Canada",
        "MX" to "Mexico",
        "GT" to "Guatemala",
        "BZ" to "Belize",
        "SV" to "El Salvador",
        "HN" to "Honduras",
        "NI" to "Nicaragua",
        "CR" to "Costa Rica",
        "PA" to "Panama",
        "CO" to "Colombia",
        "VE" to "Venezuela",
        "GY" to "Guyana",
        "SR" to "Suriname",
        "GF" to "French Guiana",
        "EC" to "Ecuador",
        "PE" to "Peru",
        "BO" to "Bolivia",
        "PY" to "Paraguay",
        "CL" to "Chile",
        "AR" to "Argentina",
        "UY" to "Uruguay",
        "BR" to "Brazil",
        "FK" to "Falkland Islands",
        "GS" to "South Georgia and the South Sandwich Islands",
        "AQ" to "Antarctica",
        "GB" to "United Kingdom",
        "IE" to "Ireland",
        "FR" to "France",
        "ES" to "Spain",
        "PT" to "Portugal",
        "DE" to "Germany",
        "NL" to "Netherlands",
        "BE" to "Belgium",
        "LU" to "Luxembourg",
        "CH" to "Switzerland",
        "AT" to "Austria",
        "IT" to "Italy",
        "DK" to "Denmark",
        "NO" to "Norway",
        "SE" to "Sweden",
        "FI" to "Finland",
        "IS" to "Iceland",
        "RU" to "Russia",
        "UA" to "Ukraine",
        "BY" to "Belarus",
        "PL" to "Poland",
        "CZ" to "Czech Republic",
        "SK" to "Slovakia",
        "HU" to "Hungary",
        "RO" to "Romania",
        "BG" to "Bulgaria",
        "HR" to "Croatia",
        "SI" to "Slovenia",
        "BA" to "Bosnia and Herzegovina",
        "RS" to "Serbia",
        "ME" to "Montenegro",
        "MK" to "North Macedonia",
        "AL" to "Albania",
        "GR" to "Greece",
        "EE" to "Estonia",
        "LV" to "Latvia",
        "LT" to "Lithuania",
        "MD" to "Moldova",
        "XK" to "Kosovo",
        "AD" to "Andorra",
        "MC" to "Monaco",
        "LI" to "Liechtenstein",
        "SM" to "San Marino",
        "VA" to "Vatican City",
        "MT" to "Malta",
        "GI" to "Gibraltar",
        "FO" to "Faroe Islands",
        "AX" to "Åland Islands",
        "SJ" to "Svalbard and Jan Mayen",
        "GL" to "Greenland"
    )

    fun getAvailableLanguages(): List<Pair<String, String>> = listOf(
        "en" to "English",
        "es" to "Spanish",
        "fr" to "French",
        "de" to "German",
        "it" to "Italian",
        "pt" to "Portuguese",
        "ru" to "Russian",
        "ja" to "Japanese",
        "ko" to "Korean",
        "zh" to "Chinese (Simplified)",
        "zh-TW" to "Chinese (Traditional)",
        "ar" to "Arabic",
        "hi" to "Hindi",
        "bn" to "Bengali",
        "pa" to "Punjabi",
        "ta" to "Tamil",
        "te" to "Telugu",
        "mr" to "Marathi",
        "ur" to "Urdu",
        "tr" to "Turkish",
        "vi" to "Vietnamese",
        "th" to "Thai",
        "id" to "Indonesian",
        "ms" to "Malay",
        "tl" to "Tagalog",
        "pl" to "Polish",
        "uk" to "Ukrainian",
        "ro" to "Romanian",
        "nl" to "Dutch",
        "el" to "Greek",
        "cs" to "Czech",
        "hu" to "Hungarian",
        "sv" to "Swedish",
        "da" to "Danish",
        "fi" to "Finnish",
        "no" to "Norwegian",
        "he" to "Hebrew",
        "fa" to "Persian",
        "sw" to "Swahili",
        "af" to "Afrikaans",
        "sq" to "Albanian",
        "hy" to "Armenian",
        "az" to "Azerbaijani",
        "eu" to "Basque",
        "be" to "Belarusian",
        "bg" to "Bulgarian",
        "ca" to "Catalan",
        "hr" to "Croatian",
        "et" to "Estonian",
        "gl" to "Galician",
        "ka" to "Georgian",
        "gu" to "Gujarati",
        "ht" to "Haitian Creole",
        "ha" to "Hausa",
        "is" to "Icelandic",
        "ig" to "Igbo",
        "ga" to "Irish",
        "jv" to "Javanese",
        "kn" to "Kannada",
        "kk" to "Kazakh",
        "km" to "Khmer",
        "ky" to "Kyrgyz",
        "lo" to "Lao",
        "lv" to "Latvian",
        "lt" to "Lithuanian",
        "lb" to "Luxembourgish",
        "mk" to "Macedonian",
        "mg" to "Malagasy",
        "ml" to "Malayalam",
        "mt" to "Maltese",
        "mi" to "Maori",
        "mr" to "Marathi",
        "mn" to "Mongolian",
        "ne" to "Nepali",
        "ps" to "Pashto",
        "sr" to "Serbian",
        "si" to "Sinhala",
        "sk" to "Slovak",
        "sl" to "Slovenian",
        "so" to "Somali",
        "su" to "Sundanese",
        "tg" to "Tajik",
        "tt" to "Tatar",
        "te" to "Telugu",
        "bo" to "Tibetan",
        "tk" to "Turkmen",
        "ug" to "Uyghur",
        "uz" to "Uzbek",
        "cy" to "Welsh",
        "fy" to "Western Frisian",
        "xh" to "Xhosa",
        "yi" to "Yiddish",
        "yo" to "Yoruba",
        "zu" to "Zulu"
    )
}