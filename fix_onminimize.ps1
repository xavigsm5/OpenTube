$file = "app\src\main\java\com\opentube\ui\screens\player\VideoPlayerScreen.kt"
$content = Get-Content $file -Raw -Encoding UTF8

# Cambio 1: Línea ~538 - Agregar thumbnailUrl al primer onMinimize
$pattern1 = '(?s)(onMinimize\(\s+state\.videoDetails\.title,\s+state\.videoDetails\.uploader,\s+)(isPlaying,)'
$replacement1 = '${1}state.videoDetails.thumbnailUrl,' + "`r`n" + '                                                $2'
$content = $content -replace $pattern1, $replacement1

# Cambio 2: Línea ~701 - Agregar thumbnailUrl al segundo onMinimize  
$pattern2 = '(?s)(onMinimize\(\s+videoDetails\.title,\s+videoDetails\.uploader,\s+)(isPlaying,)'
$replacement2 = '${1}videoDetails.thumbnailUrl,' + "`r`n" + '                                    $2'
$content = $content -replace $pattern2, $replacement2

# Guardar el archivo
$content | Set-Content $file -NoNewline -Encoding UTF8

Write-Host "✅ Cambios aplicados exitosamente"
