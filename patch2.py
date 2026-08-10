import sys

with open("app/src/main/java/com/example/ui/AppViewModel.kt", "r") as f:
    content = f.read()

old_parse_end = """            } else if (trimmed.startsWith("http", ignoreCase = true)) {
                if (currentName.isNotEmpty()) {
                    result.add(
                        ParsedM3uChannel(
                            name = currentName,
                            logo = currentLogo,
                            group = currentGroup,
                            url = trimmed
                        )
                    )
                    currentName = ""
                    currentLogo = ""
                    currentGroup = ""
                }
            }
        }
        return result"""

new_parse_end = """            } else if (trimmed.startsWith("http", ignoreCase = true)) {
                if (currentName.isNotEmpty()) {
                    val lg = currentGroup.lowercase()
                    val ln = currentName.lowercase()
                    val keep = lg.contains("argentin") || lg.contains("🇦🇷") ||
                               lg.contains("noticia") || lg.contains("aire") ||
                               lg.contains("fútbol") || lg.contains("futbol") ||
                               lg.contains("deporte") || lg.contains("espn") ||
                               lg.contains("tyc") || lg.contains("fox") ||
                               lg.contains("cine") || lg.contains("serie") ||
                               lg.contains("24/7") || lg.contains("infantil") ||
                               lg.contains("música") || lg.contains("music") ||
                               ln.contains("argentin") || ln.contains("arg ")

                    val reject = lg.contains("chile") || lg.contains("🇨🇱") ||
                                 lg.contains("uruguay") || lg.contains("🇺🇾") ||
                                 lg.contains("mexico") || lg.contains("méxico") || lg.contains("🇲🇽") ||
                                 lg.contains("españa") || lg.contains("espan") || lg.contains("🇪🇸") ||
                                 lg.contains("colombia") || lg.contains("🇨🇴") ||
                                 lg.contains("peru") || lg.contains("🇵🇪") ||
                                 lg.contains("brasil") || lg.contains("🇧🇷") ||
                                 lg.contains("usa") || lg.contains("🇺🇸") ||
                                 lg.contains("adulto") || lg.contains("xxx") ||
                                 lg.contains("ecuador") || lg.contains("bolivia") || lg.contains("venezuela") || lg.contains("paraguay")

                    if (keep && !reject) {
                        result.add(
                            ParsedM3uChannel(
                                name = currentName,
                                logo = currentLogo,
                                group = if (currentGroup.isBlank()) "Otros" else currentGroup,
                                url = trimmed
                            )
                        )
                    }
                    currentName = ""
                    currentLogo = ""
                    currentGroup = ""
                }
            }
        }
        return result"""
if old_parse_end in content:
    content = content.replace(old_parse_end, new_parse_end)
    with open("app/src/main/java/com/example/ui/AppViewModel.kt", "w") as f:
        f.write(content)
    print("Replaced successfully")
else:
    print("String not found")

