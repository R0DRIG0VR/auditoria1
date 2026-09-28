# Exposición: Mockito — Grupo 10

Auditoría de Sistemas (UMSS) · 29/09/2026, 08:15–08:44
Main Rocabado Jairo Gael · Banda Tellez Fernando · Velasquez Ricaldez Rodrigo

| Carpeta | Contenido |
|---|---|
| `informe/` | Informe en LaTeX (`informe.tex` → `informe.pdf`, 25 págs., formato defensa): Mockito y su arquitectura · **SRS** · **SDD** · diseño de casos de prueba **paso a paso** (rojo→verde, caja negra, caja blanca). La versión larga (79 págs.) quedó en `informe/version_extendida/` |
| `diapositivas/` | Diapositivas Beamer 16:9 (`diapositivas.tex` → `diapositivas.pdf`): 20 diapositivas de contenido (+ portada, contenido y cierre), con la demo como resultados reales (salida de la terminal y capturas del reporte JaCoCo en `diapositivas/img/`) |
| `ejemplos-mockito/` | Proyecto Java: 65 pruebas en verde + 3 en rojo a propósito (paso a paso), API y arquitectura de Mockito, caja negra y caja blanca |

Recompilar los PDF: `latexmk -pdf informe.tex` / `latexmk -pdf diapositivas.tex` (o subir el .tex a Overleaf).
Ejecutar los ejemplos: `cd ejemplos-mockito && ./run.sh cobertura`.
