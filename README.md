# gerber2png

### Converts Gerber files from KiCad to PNG files for Fab Modules 

For makers visiting a fablab that uses fabmodules for PCB manufacturing.
The toolchain is expecting you to use Cadsoft Eagle for your PCB designs.
Eagle has a png export that plays nice with the fabmodules png2rml.

Unfortunately, KiCad users a left stranded. This project hopes to help
those using it to generate the right files to mill your PCB with fabmodules.

Export your pcb design from KiCad to the industry standard Gerber format.
gerber2png then takes your gerber files and greates two png files from it.
One png is for milling the traces, the other one is for the holes and outline.

