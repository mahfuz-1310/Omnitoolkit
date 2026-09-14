sed -i '/import androidx.compose.foundation.lazy.items/d' app/src/main/java/com/example/ui/screens/CommonUI.kt
sed -i '/import androidx.compose.foundation.layout.\*/d' app/src/main/java/com/example/ui/screens/CommonUI.kt
sed -i '/package com.example.ui.screens/a\
import androidx.compose.foundation.lazy.items\
import androidx.compose.foundation.layout.*' app/src/main/java/com/example/ui/screens/CommonUI.kt
