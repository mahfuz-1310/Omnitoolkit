sed -i -e '/SessionCache.resetNames()/c\                        onClick = { SessionCache.resetNames(); SessionCache.resetMiddleNames(); Toast.makeText(context, "Session history reset!", Toast.LENGTH_SHORT).show() },' \
       -e '/Text("Reset Generated Results")/c\                        Text("Reset Generated Results & Middle Names")' app/src/main/java/com/example/ui/screens/NameGenScreen.kt
