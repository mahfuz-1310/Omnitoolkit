sed -i -e 's/NicknameCard(nick = nick, viewModel = viewModel, context = context)/NicknameCard(nick = nick, fontStyle = fontStyle, viewModel = viewModel, context = context)/' \
       -e 's/fun NicknameCard(nick: String,/fun NicknameCard(nick: String, fontStyle: String,/' \
       -e '/text = nick,/c\                    text = com.example.utils.FontStyles.apply(nick, fontStyle),' \
       -e '/ClipData.newPlainText("Nickname", nick)/c\                val clip = ClipData.newPlainText("Nickname", com.example.utils.FontStyles.apply(nick, fontStyle))' \
       -e '/val isFav by viewModel.isFavorite("NICKNAME", nick)/c\    val isFav by viewModel.isFavorite("NICKNAME", com.example.utils.FontStyles.apply(nick, fontStyle)).collectAsStateWithLifecycle(initialValue = false)' \
       -e '/if (isFav) viewModel.removeFavorite("NICKNAME", nick)/c\                if (isFav) viewModel.removeFavorite("NICKNAME", com.example.utils.FontStyles.apply(nick, fontStyle))' \
       -e '/else viewModel.addFavorite("NICKNAME", nick)/c\                else viewModel.addFavorite("NICKNAME", com.example.utils.FontStyles.apply(nick, fontStyle))' app/src/main/java/com/example/ui/screens/NicknameGenScreen.kt
