// ==============================================================================
// File      : CGL/src/common/image_loader.hpp
// Subject   : Common | BMP Loader & Writer
// Topics    : [TOPIC: BMP Loader] [TOPIC: 24-bit BMP Texture Mapping] [TOPIC: BGR-to-RGB Conversion]
// Game Role : Declares zero-dependency 24/32-bit uncompressed BMP I/O for AIRSTRIKE textures and exports.
// ==============================================================================

#ifndef IMAGE_LOADER_HPP
#define IMAGE_LOADER_HPP

#include <string>
#include <vector>

namespace CGL {

// [TOPIC: BMP Loader / 24-bit BMP Texture Mapping]
// Purpose: Loads an uncompressed 24-bit or 32-bit BMP file into an OpenGL-compatible RGB buffer.
// Game Role: Supplies local hull and planetary textures (space_hull.bmp, alien_surface.bmp) in CO6 scenes.
bool loadBMP(const std::string& filepath, int& width, int& height, std::vector<unsigned char>& rgbData);

// [TOPIC: 24-bit BMP Export]
// Purpose: Serializes a raw RGB byte buffer to an uncompressed 24-bit Windows BMP file.
// Game Role: Enables saving generated texture buffers or rendered images from AIRSTRIKE.
bool saveBMP(const std::string& filepath, int width, int height, const std::vector<unsigned char>& rgbData);

} // namespace CGL

#endif // IMAGE_LOADER_HPP
