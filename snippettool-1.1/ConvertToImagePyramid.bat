@echo off
set vipsbin=C:\Snippets\vips-7.18.2\bin

set infile=%1
set outfile=%infile:~0,-4%

@echo on
%vipsbin%\vips im_vips2tiff %infile% %outfile%.tif:deflate,tile:256x256,pyramid
