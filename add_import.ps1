# Script para agregar import de graphicsLayer
$file = "app\src\main\java\com\opentube\ui\screens\player\VideoPlayerScreen.kt"
$content = Get-Content $file -Raw -Encoding UTF8

# Agregar import después de "import androidx.compose.ui.draw.alpha"
$pattern = '(import androidx\.compose\.ui\.draw\.alpha\r?\n)'
$replacement = '$1import androidx.compose.ui.graphics.graphicsLayer' + "`r`n"
$content = $content -replace $pattern, $replacement

# Guardar
$content | Set-Content $file -NoNewline -Encoding UTF8

Write-Host "✅ Import agregado"
