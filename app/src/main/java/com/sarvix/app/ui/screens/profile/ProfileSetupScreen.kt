package com.sarvix.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sarvix.app.data.model.MoodStatus
import com.sarvix.app.ui.components.GradientButton
import com.sarvix.app.ui.components.getMoodColor
import com.sarvix.app.ui.theme.*
import com.sarvix.app.utils.Resource
import com.sarvix.app.viewmodel.ProfileViewModel

enum class SetupStep(val title: String, val description: String) {
    BASIC("Basic Info", "Tell us about yourself"),
    INTERESTS("Interests", "Pick at least 3 interests"),
    COUNTRY("Country", "Where are you from?"),
    LANGUAGE("Language", "What language do you speak?"),
    REVIEW("Review", "Everything look good?")
}

@Composable
fun ProfileSetupScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var currentStep by remember { mutableStateOf(SetupStep.BASIC) }
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf(MoodStatus.NEUTRAL) }
    var selectedInterests by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedCountry by remember { mutableStateOf<Pair<String, String>?>(null) }
    var selectedLanguage by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(updateState) {
        if (updateState is Resource.Success) {
            navController.navigate("main") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // Step Indicator
        StepProgressIndicator(
            steps = SetupStep.entries,
            currentStep = currentStep,
            onStepClick = { step ->
                // Allow going back to previous steps
                if (step.ordinal < currentStep.ordinal) {
                    currentStep = step
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step Content
        Box(modifier = Modifier.weight(1f)) {
            when (currentStep) {
                SetupStep.BASIC -> BasicInfoStep(
                    displayName = displayName,
                    onDisplayNameChange = { displayName = it },
                    bio = bio,
                    onBioChange = { bio = it },
                    selectedMood = selectedMood,
                    onMoodSelected = { selectedMood = it }
                )
                SetupStep.INTERESTS -> InterestsStep(
                    availableInterests = viewModel.getAvailableInterests(),
                    selectedInterests = selectedInterests,
                    onInterestsSelected = { selectedInterests = it }
                )
                SetupStep.COUNTRY -> CountryStep(
                    countries = viewModel.getAvailableCountries(),
                    selectedCountry = selectedCountry,
                    onCountrySelected = { selectedCountry = it }
                )
                SetupStep.LANGUAGE -> LanguageStep(
                    languages = viewModel.getAvailableLanguages(),
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { selectedLanguage = it }
                )
                SetupStep.REVIEW -> ReviewStep(
                    displayName = displayName,
                    bio = bio,
                    mood = selectedMood,
                    interests = selectedInterests,
                    country = selectedCountry?.second ?: "",
                    language = selectedLanguage?.second ?: ""
                )
            }
        }

        // Error
        if (updateState is Resource.Error) {
            Text(
                text = (updateState as Resource.Error).message ?: "Update failed",
                color = Error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        // Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentStep != SetupStep.BASIC) {
                OutlinedButton(
                    onClick = {
                        val steps = SetupStep.entries
                        val prevIndex = steps.indexOf(currentStep) - 1
                        if (prevIndex >= 0) {
                            currentStep = steps[prevIndex]
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurfaceVariant)
                ) {
                    Text("Back")
                }
            }

            val canProceed = when (currentStep) {
                SetupStep.BASIC -> displayName.isNotEmpty()
                SetupStep.INTERESTS -> selectedInterests.size >= 3
                SetupStep.COUNTRY -> selectedCountry != null
                SetupStep.LANGUAGE -> selectedLanguage != null
                SetupStep.REVIEW -> true
            }

            GradientButton(
                onClick = {
                    if (currentStep == SetupStep.REVIEW) {
                        viewModel.completeProfileSetup(
                            displayName = displayName,
                            bio = bio,
                            mood = selectedMood,
                            interests = selectedInterests,
                            country = selectedCountry?.second ?: "",
                            countryCode = selectedCountry?.first ?: "",
                            language = selectedLanguage?.second ?: "",
                            languageCode = selectedLanguage?.first ?: ""
                        )
                    } else {
                        val steps = SetupStep.entries
                        val nextIndex = steps.indexOf(currentStep) + 1
                        if (nextIndex < steps.size) {
                            currentStep = steps[nextIndex]
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = canProceed
            ) {
                Text(if (currentStep == SetupStep.REVIEW) "Complete Setup" else "Next")
            }
        }
    }
}

// === Step 1: Basic Info (Display Name + Bio + Mood) ===
@Composable
fun BasicInfoStep(
    displayName: String,
    onDisplayNameChange: (String) -> Unit,
    bio: String,
    onBioChange: (String) -> Unit,
    selectedMood: MoodStatus,
    onMoodSelected: (MoodStatus) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = SetupStep.BASIC.title,
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )
            Text(
                text = SetupStep.BASIC.description,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Display Name
            OutlinedTextField(
                value = displayName,
                onValueChange = onDisplayNameChange,
                label = { Text("Display Name *") },
                placeholder = { Text("How should we call you?") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryLight,
                    unfocusedBorderColor = DividerColor
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = onBioChange,
                label = { Text("Bio") },
                placeholder = { Text("Tell us a bit about yourself...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryLight,
                    unfocusedBorderColor = DividerColor
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Mood Selection
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(MoodStatus.entries.chunked(4)) { rowMoods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowMoods.forEach { mood ->
                    val isSelected = selectedMood == mood
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onMoodSelected(mood) }
                            .padding(4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) getMoodColor(mood).copy(alpha = 0.2f)
                                else Surface
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = mood.emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mood.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) getMoodColor(mood) else OnSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// === Step 2: Interests (min 3 required) ===
@Composable
fun InterestsStep(
    availableInterests: List<String>,
    selectedInterests: List<String>,
    onInterestsSelected: (List<String>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = SetupStep.INTERESTS.title,
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
        )
        Text(
            text = SetupStep.INTERESTS.description,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant
        )
        Text(
            text = "${selectedInterests.size}/10 selected (minimum 3)",
            style = MaterialTheme.typography.labelMedium,
            color = if (selectedInterests.size >= 3) AccentCyan else Error
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(availableInterests.chunked(3)) { rowInterests ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowInterests.forEach { interest ->
                        val isSelected = selectedInterests.contains(interest)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Primary else SurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    if (isSelected) {
                                        onInterestsSelected(selectedInterests - interest)
                                    } else if (selectedInterests.size < 10) {
                                        onInterestsSelected(selectedInterests + interest)
                                    }
                                }
                        ) {
                            Text(
                                text = interest,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) OnPrimary else OnSurfaceVariant,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    // Fill remaining slots in row
                    repeat(3 - rowInterests.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// === Step 3: Country Selection with Search ===
@Composable
fun CountryStep(
    countries: List<Pair<String, String>>,
    selectedCountry: Pair<String, String>?,
    onCountrySelected: (Pair<String, String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCountries = remember(searchQuery) {
        if (searchQuery.isEmpty()) countries
        else countries.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = SetupStep.COUNTRY.title,
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
        )
        Text(
            text = SetupStep.COUNTRY.description,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search countries...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryLight,
                unfocusedBorderColor = DividerColor
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Country List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(filteredCountries) { country ->
                val (code, name) = country
                val isSelected = selectedCountry == country
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Primary.copy(alpha = 0.2f) else Surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCountrySelected(country) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) OnSurface else OnSurface
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = PrimaryLight
                            )
                        }
                    }
                }
            }
        }
    }
}

// === Step 4: Language Selection with Search ===
@Composable
fun LanguageStep(
    languages: List<Pair<String, String>>,
    selectedLanguage: Pair<String, String>?,
    onLanguageSelected: (Pair<String, String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isEmpty()) languages
        else languages.filter { it.second.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = SetupStep.LANGUAGE.title,
            style = MaterialTheme.typography.headlineMedium,
            color = OnSurface
        )
        Text(
            text = SetupStep.LANGUAGE.description,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search languages...") },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryLight,
                unfocusedBorderColor = DividerColor
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Language List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(filteredLanguages) { language ->
                val (code, name) = language
                val isSelected = selectedLanguage == language
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Primary.copy(alpha = 0.2f) else Surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(language) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) PrimaryLight else OnSurface
                        )
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = PrimaryLight
                            )
                        }
                    }
                }
            }
        }
    }
}

// === Step 5: Review and Submit ===
@Composable
fun ReviewStep(
    displayName: String,
    bio: String,
    mood: MoodStatus,
    interests: List<String>,
    country: String,
    language: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = SetupStep.REVIEW.title,
                style = MaterialTheme.typography.headlineMedium,
                color = OnSurface
            )
            Text(
                text = SetupStep.REVIEW.description,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Review Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Display Name
                    ReviewItem("Display Name", displayName)
                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    // Bio
                    ReviewItem("Bio", bio.ifEmpty { "Not provided" })
                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    // Mood
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mood",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant,
                            modifier = Modifier.width(120.dp)
                        )
                        Text(
                            text = "${mood.emoji} ${mood.displayName}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = getMoodColor(mood)
                        )
                    }
                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    // Interests
                    Text(
                        text = "Interests (${interests.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    com.sarvix.app.ui.components.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        interests.forEach { interest ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Primary.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = interest,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    // Country
                    ReviewItem("Country", country)
                    Divider(color = DividerColor, modifier = Modifier.padding(vertical = 12.dp))

                    // Language
                    ReviewItem("Language", language)
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurface
        )
    }
}

// === Step Progress Indicator ===
@Composable
fun StepProgressIndicator(
    steps: List<SetupStep>,
    currentStep: SetupStep,
    onStepClick: (SetupStep) -> Unit
) {
    Surface(
        color = Surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                val isActive = step == currentStep
                val isCompleted = step.ordinal < currentStep.ordinal

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            enabled = step.ordinal < currentStep.ordinal,
                            onClick = { onStepClick(step) }
                        )
                ) {
                    Surface(
                        shape = CircleShape,
                        color = when {
                            isActive -> Primary
                            isCompleted -> PrimaryLight
                            else -> SurfaceVariant
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCompleted) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = OnPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isActive) OnPrimary else OnSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isActive -> PrimaryLight
                            isCompleted -> OnSurfaceVariant
                            else -> OnSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .padding(top = 16.dp)
                            .background(
                                if (isCompleted) PrimaryLight else DividerColor
                            )
                    )
                }
            }
        }
    }
}
