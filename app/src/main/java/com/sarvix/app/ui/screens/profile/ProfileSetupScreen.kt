@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package com.sarvix.app.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.ui.components.FlowRow
import com.sarvix.app.ui.components.getMoodColor
import com.sarvix.app.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileViewModel,
    onSetupComplete: () -> Unit
) {
    val updateState by viewModel.updateState.collectAsState()
    
    var currentStep by remember { mutableStateOf(0) }
    
    // Form data
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(MoodStatus.NEUTRAL) }
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var selectedCountry by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedLanguage by remember { mutableStateOf<Pair<String, String>?>(null) }
    
    val availableInterests by viewModel.availableInterests.collectAsState()
    val availableCountries by viewModel.availableCountries.collectAsState()
    val availableLanguages by viewModel.availableLanguages.collectAsState()
    
    // Handle completion
    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            onSetupComplete()
        }
    }
    
    Scaffold(
        topBar = {
            com.sarvix.app.ui.components.PillHeader(
                title = "Complete Your Profile"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Progress Indicator
            LinearProgressIndicator(
                progress = { (currentStep + 1) / 5f },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Step Content
            when (currentStep) {
                0 -> BasicInfoStep(
                    displayName = displayName,
                    onDisplayNameChange = { displayName = it },
                    bio = bio,
                    onBioChange = { bio = it },
                    selectedMood = selectedMood,
                    onMoodSelect = { selectedMood = it }
                )
                1 -> InterestsStep(
                    availableInterests = availableInterests,
                    selectedInterests = selectedInterests,
                    onInterestToggle = { interest ->
                        selectedInterests = if (selectedInterests.contains(interest)) {
                            selectedInterests - interest
                        } else {
                            selectedInterests + interest
                        }
                    }
                )
                2 -> CountryStep(
                    availableCountries = availableCountries,
                    selectedCountry = selectedCountry,
                    onCountrySelect = { selectedCountry = it }
                )
                3 -> LanguageStep(
                    availableLanguages = availableLanguages,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelect = { selectedLanguage = it }
                )
                4 -> ReviewStep(
                    displayName = displayName,
                    bio = bio,
                    selectedMood = selectedMood,
                    selectedInterests = selectedInterests,
                    selectedCountry = selectedCountry,
                    selectedLanguage = selectedLanguage
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    OutlinedButton(onClick = { currentStep-- }) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                
                com.sarvix.app.ui.components.GradientButton(
                    text = if (currentStep < 4) "Next" else "Complete",
                    onClick = {
                        if (currentStep < 4) {
                            currentStep++
                        } else {
                            // Submit profile
                            viewModel.updateProfile(
                                displayName = displayName.takeIf { it.isNotBlank() },
                                bio = bio.takeIf { it.isNotBlank() },
                                mood = selectedMood,
                                interests = selectedInterests.toList(),
                                country = selectedCountry?.second,
                                countryCode = selectedCountry?.first,
                                language = selectedLanguage?.second,
                                languageCode = selectedLanguage?.first
                            )
                        }
                    },
                    enabled = when (currentStep) {
                        0 -> displayName.isNotBlank()
                        1 -> selectedInterests.size >= 3
                        2 -> selectedCountry != null
                        3 -> selectedLanguage != null
                        4 -> true
                        else -> false
                    },
                    loading = updateState is Resource.Loading
                )
            }
            
            // Error Message
            if (updateState is Resource.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (updateState as Resource.Error).message ?: "Failed to save profile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun BasicInfoStep(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    selectedMood: MoodStatus,
    onMoodSelect: (MoodStatus) -> Unit
) {
    Column {
        Text(
            text = "Tell us about yourself",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Display Name
        OutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            label = { Text("Display Name *") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bio
        OutlinedTextField(
            value = bio,
            onValueChange = onBioChange,
            label = { Text("Bio (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
            supportingText = { Text("${bio.length}/500") }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Mood Selection
        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MoodStatus.values().forEach { mood ->
                val isSelected = mood == selectedMood
                FilterChip(
                    selected = isSelected,
                    onClick = { onMoodSelect(mood) },
                    label = { Text("${mood.emoji} ${mood.displayName}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getMoodColor(mood).copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}

@Composable
fun InterestsStep(
    availableInterests: List<String>,
    selectedInterests: Set<String>,
    onInterestToggle: (String) -> Unit
) {
    Column {
        Text(
            text = "Select Your Interests",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Choose at least 3 interests (${selectedInterests.size} selected)",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableInterests.chunked(2)) { rowInterests ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowInterests.forEach { interest ->
                        val isSelected = selectedInterests.contains(interest)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onInterestToggle(interest) },
                            label = { Text(interest) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowInterests.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CountryStep(
    availableCountries: List<Pair<String, String>>,
    selectedCountry: Pair<String, String>?,
    onCountrySelect: (Pair<String, String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredCountries = remember(searchQuery, availableCountries) {
        if (searchQuery.isBlank()) {
            availableCountries
        } else {
            availableCountries.filter { it.second.contains(searchQuery, ignoreCase = true) }
        }
    }
    
    Column {
        Text(
            text = "Select Your Country",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search countries") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Country List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredCountries) { country ->
                val isSelected = selectedCountry == country
                ListItem(
                    headlineContent = { Text(country.second) },
                    leadingContent = {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onCountrySelect(country) }
                        )
                    },
                    modifier = Modifier.clickable { onCountrySelect(country) }
                )
            }
        }
    }
}

@Composable
fun LanguageStep(
    availableLanguages: List<Pair<String, String>>,
    selectedLanguage: Pair<String, String>?,
    onLanguageSelect: (Pair<String, String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredLanguages = remember(searchQuery, availableLanguages) {
        if (searchQuery.isBlank()) {
            availableLanguages
        } else {
            availableLanguages.filter { it.second.contains(searchQuery, ignoreCase = true) }
        }
    }
    
    Column {
        Text(
            text = "Select Your Language",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search languages") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Language List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredLanguages) { language ->
                val isSelected = selectedLanguage == language
                ListItem(
                    headlineContent = { Text(language.second) },
                    leadingContent = {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onLanguageSelect(language) }
                        )
                    },
                    modifier = Modifier.clickable { onLanguageSelect(language) }
                )
            }
        }
    }
}

@Composable
fun ReviewStep(
    displayName: String,
    bio: String,
    selectedMood: MoodStatus,
    selectedInterests: Set<String>,
    selectedCountry: Pair<String, String>?,
    selectedLanguage: Pair<String, String>?
) {
    Column {
        Text(
            text = "Review Your Profile",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Profile Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Display Name
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge
                )
                
                // Mood
                Text(
                    text = "${selectedMood.emoji} ${selectedMood.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Bio
                if (bio.isNotEmpty()) {
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Country & Language
                Row {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedCountry?.second ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Row {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = selectedLanguage?.second ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Interests
                Text(
                    text = "Interests:",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedInterests.forEach { interest ->
                        AssistChip(
                            onClick = {},
                            label = { Text(interest) }
                        )
                    }
                }
            }
        }
    }
}
