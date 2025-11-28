package com.eka.medassist.ui.chat.presentation.components

//@Composable
//fun DefaultSuggestionsComponent(
//    onSuggestionClicked: (SuggestionModel) -> Unit,
//) {
//    var defaultSuggestions by remember { mutableStateOf(emptyList<SuggestionModel>()) }
//    var name by remember { mutableStateOf("") }
//
//    Column(
//        modifier = Modifier
//            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
//        verticalArrangement = Arrangement.Top
//    ) {
//        Text(
//            text = "Hello $name, how can I help you today?",
//            style = touchBodyRegular.copy(color = DarwinTouchNeutral600)
//        )
//        Spacer(modifier = Modifier.height(16.dp))
//        defaultSuggestions.forEach { suggestion ->
//            SingleSelectSuggestion(suggestion = suggestion) {
//                onSuggestionClicked(suggestion)
//            }
//            Spacer(modifier = Modifier.height(8.dp))
//        }
//    }
//}