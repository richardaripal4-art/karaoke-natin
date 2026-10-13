# KARAOKE NATIN — Android starter project

## Kasama sa starter
- Kotlin + Jetpack Compose interface
- Search ng title/singer (ang singer ay default na `Unknown singer` para sa local files)
- Pagpili ng local audio/video files gamit ang Android file picker
- Playback gamit ang Media3 ExoPlayer
- Queue: magdagdag, mag-play, magtanggal
- Lyrics dialog placeholder
- Pagdagdag ng direktang online media URL

## Mga limitasyon ng unang bersyon
- Hindi pa naka-save ang library at queue pagkatapos isara ang app.
- Hindi pa nababago sa UI ang singer/title ng imported local file.
- Lyrics ay placeholder; kailangan pa ng LRC/SRT import o paglagay ng lyrics na may pahintulot.
- Ang ordinaryong YouTube watch URL ay hindi direktang audio/video stream. Gumamit lamang ng awtorisadong integration o direktang media URL na pinapayagan ng provider.
- Hindi kasama ang copyrighted songs; mag-import lamang ng files na pag-aari mo o may pahintulot kang gamitin.

## Paano gumawa ng APK
1. Mag-install ng Android Studio sa Windows, Mac, o Linux computer.
2. I-extract ang ZIP.
3. Sa Android Studio, piliin ang **Open** at piliin ang folder na `KARAOKE_NATIN`.
4. Hintaying matapos ang Gradle sync. Kung hingin, mag-install ng Android SDK Platform 35.
5. Piliin ang **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. Kapag matagumpay, piliin ang **locate** sa notification para makita ang APK.
7. Ilipat ang APK sa Android phone at i-install. Maaaring kailanganing payagan ang pag-install mula sa source na iyon.

## Build requirements
- Android Studio version supporting AGP 8.7.3
- JDK 17
- Android SDK Platform 35
