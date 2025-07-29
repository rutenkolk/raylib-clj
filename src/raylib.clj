(ns
 raylib
 (:require
  [clojure.java.io :as io]
  [clojure.string :as s]
  [clojure.set :as sets]
  [clojure.pprint :as pprint]
  [clojure.edn :as edn]
  [coffi.ffi :as ffi]
  [coffi.mem :as mem]
  [coffi.layout :as layout]
  [coffimaker.runtime :as runtime])
 (:import
  (clojure.lang IDeref IFn IMeta IObj IReference)
  (java.lang.invoke MethodHandle MethodHandles MethodType)
  (java.lang.foreign
   Linker
   Linker$Option
   FunctionDescriptor
   AddressLayout
   Arena
   MemoryLayout
   MemorySegment
   MemorySegment$Scope
   SegmentAllocator
   ValueLayout
   ValueLayout$OfByte
   ValueLayout$OfShort
   ValueLayout$OfInt
   ValueLayout$OfLong
   ValueLayout$OfChar
   ValueLayout$OfFloat
   ValueLayout$OfDouble)
  (java.nio ByteOrder)))

(set! clojure.core/*warn-on-reflection* true)

(coffi.ffi/load-library "raylib.dll")

(coffi.mem/defalias :raylib/va_list :coffi.mem/c-string)

(coffi.mem/defstruct Vector2 [x :coffi.mem/float y :coffi.mem/float])

(coffi.mem/defstruct
 Vector3
 [x :coffi.mem/float y :coffi.mem/float z :coffi.mem/float])

(coffi.mem/defstruct
 Vector4
 [x
  :coffi.mem/float
  y
  :coffi.mem/float
  z
  :coffi.mem/float
  w
  :coffi.mem/float])

(coffi.mem/defstruct
 Quaternion
 [x
  :coffi.mem/float
  y
  :coffi.mem/float
  z
  :coffi.mem/float
  w
  :coffi.mem/float])

(coffi.mem/defstruct
 Matrix
 [m0
  :coffi.mem/float
  m4
  :coffi.mem/float
  m8
  :coffi.mem/float
  m12
  :coffi.mem/float
  m1
  :coffi.mem/float
  m5
  :coffi.mem/float
  m9
  :coffi.mem/float
  m13
  :coffi.mem/float
  m2
  :coffi.mem/float
  m6
  :coffi.mem/float
  m10
  :coffi.mem/float
  m14
  :coffi.mem/float
  m3
  :coffi.mem/float
  m7
  :coffi.mem/float
  m11
  :coffi.mem/float
  m15
  :coffi.mem/float])

(coffi.mem/defstruct
 Color
 [r
  :coffi.mem/byte
  g
  :coffi.mem/byte
  b
  :coffi.mem/byte
  a
  :coffi.mem/byte])

(coffi.mem/defstruct
 Rectangle
 [x
  :coffi.mem/float
  y
  :coffi.mem/float
  width
  :coffi.mem/float
  height
  :coffi.mem/float])

(coffi.mem/defstruct
 Image
 [data
  :coffi.mem/pointer
  width
  :coffi.mem/int
  height
  :coffi.mem/int
  mipmaps
  :coffi.mem/int
  format
  :coffi.mem/int])

(coffi.mem/defstruct
 Texture
 [id
  :coffi.mem/int
  width
  :coffi.mem/int
  height
  :coffi.mem/int
  mipmaps
  :coffi.mem/int
  format
  :coffi.mem/int])

(coffi.mem/defstruct
 Texture2D
 [id
  :coffi.mem/int
  width
  :coffi.mem/int
  height
  :coffi.mem/int
  mipmaps
  :coffi.mem/int
  format
  :coffi.mem/int])

(coffi.mem/defstruct
 TextureCubemap
 [id
  :coffi.mem/int
  width
  :coffi.mem/int
  height
  :coffi.mem/int
  mipmaps
  :coffi.mem/int
  format
  :coffi.mem/int])

(coffi.mem/defstruct
 RenderTexture
 [id :coffi.mem/int texture :raylib/Texture depth :raylib/Texture])

(coffi.mem/defstruct
 RenderTexture2D
 [id :coffi.mem/int texture :raylib/Texture depth :raylib/Texture])

(coffi.mem/defstruct
 NPatchInfo
 [source
  :raylib/Rectangle
  left
  :coffi.mem/int
  top
  :coffi.mem/int
  right
  :coffi.mem/int
  bottom
  :coffi.mem/int
  layout
  :coffi.mem/int])

(coffi.mem/defstruct
 GlyphInfo
 [value
  :coffi.mem/int
  offsetX
  :coffi.mem/int
  offsetY
  :coffi.mem/int
  advanceX
  :coffi.mem/int
  image
  :raylib/Image])

(coffi.mem/defstruct
 Font
 [baseSize
  :coffi.mem/int
  glyphCount
  :coffi.mem/int
  glyphPadding
  :coffi.mem/int
  texture
  :raylib/Texture
  recs
  [:coffi.mem/pointer :raylib/Rectangle]
  glyphs
  [:coffi.mem/pointer :raylib/GlyphInfo]])

(coffi.mem/defstruct
 Camera3D
 [position
  :raylib/Vector3
  target
  :raylib/Vector3
  up
  :raylib/Vector3
  fovy
  :coffi.mem/float
  projection
  :coffi.mem/int])

(coffi.mem/defstruct
 Camera
 [position
  :raylib/Vector3
  target
  :raylib/Vector3
  up
  :raylib/Vector3
  fovy
  :coffi.mem/float
  projection
  :coffi.mem/int])

(coffi.mem/defstruct
 Camera2D
 [offset
  :raylib/Vector2
  target
  :raylib/Vector2
  rotation
  :coffi.mem/float
  zoom
  :coffi.mem/float])

(coffi.mem/defstruct
 Mesh
 [vertexCount
  :coffi.mem/int
  triangleCount
  :coffi.mem/int
  vertices
  [:coffi.mem/pointer :coffi.mem/float]
  texcoords
  [:coffi.mem/pointer :coffi.mem/float]
  texcoords2
  [:coffi.mem/pointer :coffi.mem/float]
  normals
  [:coffi.mem/pointer :coffi.mem/float]
  tangents
  [:coffi.mem/pointer :coffi.mem/float]
  colors
  :coffi.mem/c-string
  indices
  [:coffi.mem/pointer :coffi.mem/short]
  animVertices
  [:coffi.mem/pointer :coffi.mem/float]
  animNormals
  [:coffi.mem/pointer :coffi.mem/float]
  boneIds
  :coffi.mem/c-string
  boneWeights
  [:coffi.mem/pointer :coffi.mem/float]
  vaoId
  :coffi.mem/int
  vboId
  [:coffi.mem/pointer :coffi.mem/int]])

(coffi.mem/defstruct
 Shader
 [id :coffi.mem/int locs [:coffi.mem/pointer :coffi.mem/int]])

(coffi.mem/defstruct
 MaterialMap
 [texture :raylib/Texture color :raylib/Color value :coffi.mem/float])

(coffi.mem/defstruct
 Material
 [shader
  :raylib/Shader
  maps
  [:coffi.mem/pointer :raylib/MaterialMap]
  params
  [:coffi.mem/array :coffi.mem/float 4]])

(coffi.mem/defstruct
 Transform
 [translation
  :raylib/Vector3
  rotation
  :raylib/Vector4
  scale
  :raylib/Vector3])

(coffi.mem/defstruct
 BoneInfo
 [name [:coffi.mem/array :coffi.mem/byte 32] parent :coffi.mem/int])

(coffi.mem/defstruct
 Model
 [transform
  :raylib/Matrix
  meshCount
  :coffi.mem/int
  materialCount
  :coffi.mem/int
  meshes
  [:coffi.mem/pointer :raylib/Mesh]
  materials
  [:coffi.mem/pointer :raylib/Material]
  meshMaterial
  [:coffi.mem/pointer :coffi.mem/int]
  boneCount
  :coffi.mem/int
  bones
  [:coffi.mem/pointer :raylib/BoneInfo]
  bindPose
  [:coffi.mem/pointer :raylib/Transform]])

(coffi.mem/defstruct
 ModelAnimation
 [boneCount
  :coffi.mem/int
  frameCount
  :coffi.mem/int
  bones
  [:coffi.mem/pointer :raylib/BoneInfo]
  framePoses
  [:coffi.mem/pointer [:coffi.mem/pointer :raylib/Transform]]
  name
  [:coffi.mem/array :coffi.mem/byte 32]])

(coffi.mem/defstruct
 Ray
 [position :raylib/Vector3 direction :raylib/Vector3])

(coffi.mem/defstruct
 RayCollision
 [hit
  :coffimaker.runtime/bool
  distance
  :coffi.mem/float
  point
  :raylib/Vector3
  normal
  :raylib/Vector3])

(coffi.mem/defstruct
 BoundingBox
 [min :raylib/Vector3 max :raylib/Vector3])

(coffi.mem/defstruct
 Wave
 [frameCount
  :coffi.mem/int
  sampleRate
  :coffi.mem/int
  sampleSize
  :coffi.mem/int
  channels
  :coffi.mem/int
  data
  :coffi.mem/pointer])

(coffi.mem/defalias :raylib/rAudioBuffer :coffi.mem/void)

(coffi.mem/defalias :raylib/rAudioProcessor :coffi.mem/void)

(coffi.mem/defstruct
 AudioStream
 [buffer
  [:coffi.mem/pointer :raylib/rAudioBuffer]
  processor
  [:coffi.mem/pointer :raylib/rAudioProcessor]
  sampleRate
  :coffi.mem/int
  sampleSize
  :coffi.mem/int
  channels
  :coffi.mem/int])

(coffi.mem/defstruct
 Sound
 [stream :raylib/AudioStream frameCount :coffi.mem/int])

(coffi.mem/defstruct
 Music
 [stream
  :raylib/AudioStream
  frameCount
  :coffi.mem/int
  looping
  :coffimaker.runtime/bool
  ctxType
  :coffi.mem/int
  ctxData
  :coffi.mem/pointer])

(coffi.mem/defstruct
 VrDeviceInfo
 [hResolution
  :coffi.mem/int
  vResolution
  :coffi.mem/int
  hScreenSize
  :coffi.mem/float
  vScreenSize
  :coffi.mem/float
  vScreenCenter
  :coffi.mem/float
  eyeToScreenDistance
  :coffi.mem/float
  lensSeparationDistance
  :coffi.mem/float
  interpupillaryDistance
  :coffi.mem/float
  lensDistortionValues
  [:coffi.mem/array :coffi.mem/float 4]
  chromaAbCorrection
  [:coffi.mem/array :coffi.mem/float 4]])

(coffi.mem/defstruct
 VrStereoConfig
 [projection
  [:coffi.mem/array :raylib/Matrix 2]
  viewOffset
  [:coffi.mem/array :raylib/Matrix 2]
  leftLensCenter
  [:coffi.mem/array :coffi.mem/float 2]
  rightLensCenter
  [:coffi.mem/array :coffi.mem/float 2]
  leftScreenCenter
  [:coffi.mem/array :coffi.mem/float 2]
  rightScreenCenter
  [:coffi.mem/array :coffi.mem/float 2]
  scale
  [:coffi.mem/array :coffi.mem/float 2]
  scaleIn
  [:coffi.mem/array :coffi.mem/float 2]])

(coffi.mem/defstruct
 FilePathList
 [capacity
  :coffi.mem/int
  count
  :coffi.mem/int
  paths
  [:coffi.mem/pointer :coffi.mem/c-string]])

(def ^{:const true} FLAG_VSYNC_HINT 64)

(def ^{:const true} FLAG_FULLSCREEN_MODE 2)

(def ^{:const true} FLAG_WINDOW_RESIZABLE 4)

(def ^{:const true} FLAG_WINDOW_UNDECORATED 8)

(def ^{:const true} FLAG_WINDOW_HIDDEN 128)

(def ^{:const true} FLAG_WINDOW_MINIMIZED 512)

(def ^{:const true} FLAG_WINDOW_MAXIMIZED 1024)

(def ^{:const true} FLAG_WINDOW_UNFOCUSED 2048)

(def ^{:const true} FLAG_WINDOW_TOPMOST 4096)

(def ^{:const true} FLAG_WINDOW_ALWAYS_RUN 256)

(def ^{:const true} FLAG_WINDOW_TRANSPARENT 16)

(def ^{:const true} FLAG_WINDOW_HIGHDPI 8192)

(def ^{:const true} FLAG_WINDOW_MOUSE_PASSTHROUGH 16384)

(def ^{:const true} FLAG_BORDERLESS_WINDOWED_MODE 32768)

(def ^{:const true} FLAG_MSAA_4X_HINT 32)

(def ^{:const true} FLAG_INTERLACED_HINT 65536)

(coffi.mem/defalias :raylib/ConfigFlags :coffi.mem/int)

(def ^{:const true} LOG_ALL 0)

(def ^{:const true} LOG_TRACE 1)

(def ^{:const true} LOG_DEBUG 2)

(def ^{:const true} LOG_INFO 3)

(def ^{:const true} LOG_WARNING 4)

(def ^{:const true} LOG_ERROR 5)

(def ^{:const true} LOG_FATAL 6)

(def ^{:const true} LOG_NONE 7)

(coffi.mem/defalias :raylib/TraceLogLevel :coffi.mem/int)

(def ^{:const true} KEY_NULL 0)

(def ^{:const true} KEY_APOSTROPHE 39)

(def ^{:const true} KEY_COMMA 44)

(def ^{:const true} KEY_MINUS 45)

(def ^{:const true} KEY_PERIOD 46)

(def ^{:const true} KEY_SLASH 47)

(def ^{:const true} KEY_ZERO 48)

(def ^{:const true} KEY_ONE 49)

(def ^{:const true} KEY_TWO 50)

(def ^{:const true} KEY_THREE 51)

(def ^{:const true} KEY_FOUR 52)

(def ^{:const true} KEY_FIVE 53)

(def ^{:const true} KEY_SIX 54)

(def ^{:const true} KEY_SEVEN 55)

(def ^{:const true} KEY_EIGHT 56)

(def ^{:const true} KEY_NINE 57)

(def ^{:const true} KEY_SEMICOLON 59)

(def ^{:const true} KEY_EQUAL 61)

(def ^{:const true} KEY_A 65)

(def ^{:const true} KEY_B 66)

(def ^{:const true} KEY_C 67)

(def ^{:const true} KEY_D 68)

(def ^{:const true} KEY_E 69)

(def ^{:const true} KEY_F 70)

(def ^{:const true} KEY_G 71)

(def ^{:const true} KEY_H 72)

(def ^{:const true} KEY_I 73)

(def ^{:const true} KEY_J 74)

(def ^{:const true} KEY_K 75)

(def ^{:const true} KEY_L 76)

(def ^{:const true} KEY_M 77)

(def ^{:const true} KEY_N 78)

(def ^{:const true} KEY_O 79)

(def ^{:const true} KEY_P 80)

(def ^{:const true} KEY_Q 81)

(def ^{:const true} KEY_R 82)

(def ^{:const true} KEY_S 83)

(def ^{:const true} KEY_T 84)

(def ^{:const true} KEY_U 85)

(def ^{:const true} KEY_V 86)

(def ^{:const true} KEY_W 87)

(def ^{:const true} KEY_X 88)

(def ^{:const true} KEY_Y 89)

(def ^{:const true} KEY_Z 90)

(def ^{:const true} KEY_LEFT_BRACKET 91)

(def ^{:const true} KEY_BACKSLASH 92)

(def ^{:const true} KEY_RIGHT_BRACKET 93)

(def ^{:const true} KEY_GRAVE 96)

(def ^{:const true} KEY_SPACE 32)

(def ^{:const true} KEY_ESCAPE 256)

(def ^{:const true} KEY_ENTER 257)

(def ^{:const true} KEY_TAB 258)

(def ^{:const true} KEY_BACKSPACE 259)

(def ^{:const true} KEY_INSERT 260)

(def ^{:const true} KEY_DELETE 261)

(def ^{:const true} KEY_RIGHT 262)

(def ^{:const true} KEY_LEFT 263)

(def ^{:const true} KEY_DOWN 264)

(def ^{:const true} KEY_UP 265)

(def ^{:const true} KEY_PAGE_UP 266)

(def ^{:const true} KEY_PAGE_DOWN 267)

(def ^{:const true} KEY_HOME 268)

(def ^{:const true} KEY_END 269)

(def ^{:const true} KEY_CAPS_LOCK 280)

(def ^{:const true} KEY_SCROLL_LOCK 281)

(def ^{:const true} KEY_NUM_LOCK 282)

(def ^{:const true} KEY_PRINT_SCREEN 283)

(def ^{:const true} KEY_PAUSE 284)

(def ^{:const true} KEY_F1 290)

(def ^{:const true} KEY_F2 291)

(def ^{:const true} KEY_F3 292)

(def ^{:const true} KEY_F4 293)

(def ^{:const true} KEY_F5 294)

(def ^{:const true} KEY_F6 295)

(def ^{:const true} KEY_F7 296)

(def ^{:const true} KEY_F8 297)

(def ^{:const true} KEY_F9 298)

(def ^{:const true} KEY_F10 299)

(def ^{:const true} KEY_F11 300)

(def ^{:const true} KEY_F12 301)

(def ^{:const true} KEY_LEFT_SHIFT 340)

(def ^{:const true} KEY_LEFT_CONTROL 341)

(def ^{:const true} KEY_LEFT_ALT 342)

(def ^{:const true} KEY_LEFT_SUPER 343)

(def ^{:const true} KEY_RIGHT_SHIFT 344)

(def ^{:const true} KEY_RIGHT_CONTROL 345)

(def ^{:const true} KEY_RIGHT_ALT 346)

(def ^{:const true} KEY_RIGHT_SUPER 347)

(def ^{:const true} KEY_KB_MENU 348)

(def ^{:const true} KEY_KP_0 320)

(def ^{:const true} KEY_KP_1 321)

(def ^{:const true} KEY_KP_2 322)

(def ^{:const true} KEY_KP_3 323)

(def ^{:const true} KEY_KP_4 324)

(def ^{:const true} KEY_KP_5 325)

(def ^{:const true} KEY_KP_6 326)

(def ^{:const true} KEY_KP_7 327)

(def ^{:const true} KEY_KP_8 328)

(def ^{:const true} KEY_KP_9 329)

(def ^{:const true} KEY_KP_DECIMAL 330)

(def ^{:const true} KEY_KP_DIVIDE 331)

(def ^{:const true} KEY_KP_MULTIPLY 332)

(def ^{:const true} KEY_KP_SUBTRACT 333)

(def ^{:const true} KEY_KP_ADD 334)

(def ^{:const true} KEY_KP_ENTER 335)

(def ^{:const true} KEY_KP_EQUAL 336)

(def ^{:const true} KEY_BACK 4)

(def ^{:const true} KEY_MENU 82)

(def ^{:const true} KEY_VOLUME_UP 24)

(def ^{:const true} KEY_VOLUME_DOWN 25)

(coffi.mem/defalias :raylib/KeyboardKey :coffi.mem/int)

(def ^{:const true} MOUSE_BUTTON_LEFT 0)

(def ^{:const true} MOUSE_BUTTON_RIGHT 1)

(def ^{:const true} MOUSE_BUTTON_MIDDLE 2)

(def ^{:const true} MOUSE_BUTTON_SIDE 3)

(def ^{:const true} MOUSE_BUTTON_EXTRA 4)

(def ^{:const true} MOUSE_BUTTON_FORWARD 5)

(def ^{:const true} MOUSE_BUTTON_BACK 6)

(coffi.mem/defalias :raylib/MouseButton :coffi.mem/int)

(def ^{:const true} MOUSE_CURSOR_DEFAULT 0)

(def ^{:const true} MOUSE_CURSOR_ARROW 1)

(def ^{:const true} MOUSE_CURSOR_IBEAM 2)

(def ^{:const true} MOUSE_CURSOR_CROSSHAIR 3)

(def ^{:const true} MOUSE_CURSOR_POINTING_HAND 4)

(def ^{:const true} MOUSE_CURSOR_RESIZE_EW 5)

(def ^{:const true} MOUSE_CURSOR_RESIZE_NS 6)

(def ^{:const true} MOUSE_CURSOR_RESIZE_NWSE 7)

(def ^{:const true} MOUSE_CURSOR_RESIZE_NESW 8)

(def ^{:const true} MOUSE_CURSOR_RESIZE_ALL 9)

(def ^{:const true} MOUSE_CURSOR_NOT_ALLOWED 10)

(coffi.mem/defalias :raylib/MouseCursor :coffi.mem/int)

(def ^{:const true} GAMEPAD_BUTTON_UNKNOWN 0)

(def ^{:const true} GAMEPAD_BUTTON_LEFT_FACE_UP 1)

(def ^{:const true} GAMEPAD_BUTTON_LEFT_FACE_RIGHT 2)

(def ^{:const true} GAMEPAD_BUTTON_LEFT_FACE_DOWN 3)

(def ^{:const true} GAMEPAD_BUTTON_LEFT_FACE_LEFT 4)

(def ^{:const true} GAMEPAD_BUTTON_RIGHT_FACE_UP 5)

(def ^{:const true} GAMEPAD_BUTTON_RIGHT_FACE_RIGHT 6)

(def ^{:const true} GAMEPAD_BUTTON_RIGHT_FACE_DOWN 7)

(def ^{:const true} GAMEPAD_BUTTON_RIGHT_FACE_LEFT 8)

(def ^{:const true} GAMEPAD_BUTTON_LEFT_TRIGGER_1 9)

(def ^{:const true} GAMEPAD_BUTTON_LEFT_TRIGGER_2 10)

(def ^{:const true} GAMEPAD_BUTTON_RIGHT_TRIGGER_1 11)

(def ^{:const true} GAMEPAD_BUTTON_RIGHT_TRIGGER_2 12)

(def ^{:const true} GAMEPAD_BUTTON_MIDDLE_LEFT 13)

(def ^{:const true} GAMEPAD_BUTTON_MIDDLE 14)

(def ^{:const true} GAMEPAD_BUTTON_MIDDLE_RIGHT 15)

(def ^{:const true} GAMEPAD_BUTTON_LEFT_THUMB 16)

(def ^{:const true} GAMEPAD_BUTTON_RIGHT_THUMB 17)

(coffi.mem/defalias :raylib/GamepadButton :coffi.mem/int)

(def ^{:const true} GAMEPAD_AXIS_LEFT_X 0)

(def ^{:const true} GAMEPAD_AXIS_LEFT_Y 1)

(def ^{:const true} GAMEPAD_AXIS_RIGHT_X 2)

(def ^{:const true} GAMEPAD_AXIS_RIGHT_Y 3)

(def ^{:const true} GAMEPAD_AXIS_LEFT_TRIGGER 4)

(def ^{:const true} GAMEPAD_AXIS_RIGHT_TRIGGER 5)

(coffi.mem/defalias :raylib/GamepadAxis :coffi.mem/int)

(def ^{:const true} MATERIAL_MAP_ALBEDO 0)

(def ^{:const true} MATERIAL_MAP_METALNESS 1)

(def ^{:const true} MATERIAL_MAP_NORMAL 2)

(def ^{:const true} MATERIAL_MAP_ROUGHNESS 3)

(def ^{:const true} MATERIAL_MAP_OCCLUSION 4)

(def ^{:const true} MATERIAL_MAP_EMISSION 5)

(def ^{:const true} MATERIAL_MAP_HEIGHT 6)

(def ^{:const true} MATERIAL_MAP_CUBEMAP 7)

(def ^{:const true} MATERIAL_MAP_IRRADIANCE 8)

(def ^{:const true} MATERIAL_MAP_PREFILTER 9)

(def ^{:const true} MATERIAL_MAP_BRDF 10)

(coffi.mem/defalias :raylib/MaterialMapIndex :coffi.mem/int)

(def ^{:const true} SHADER_LOC_VERTEX_POSITION 0)

(def ^{:const true} SHADER_LOC_VERTEX_TEXCOORD01 1)

(def ^{:const true} SHADER_LOC_VERTEX_TEXCOORD02 2)

(def ^{:const true} SHADER_LOC_VERTEX_NORMAL 3)

(def ^{:const true} SHADER_LOC_VERTEX_TANGENT 4)

(def ^{:const true} SHADER_LOC_VERTEX_COLOR 5)

(def ^{:const true} SHADER_LOC_MATRIX_MVP 6)

(def ^{:const true} SHADER_LOC_MATRIX_VIEW 7)

(def ^{:const true} SHADER_LOC_MATRIX_PROJECTION 8)

(def ^{:const true} SHADER_LOC_MATRIX_MODEL 9)

(def ^{:const true} SHADER_LOC_MATRIX_NORMAL 10)

(def ^{:const true} SHADER_LOC_VECTOR_VIEW 11)

(def ^{:const true} SHADER_LOC_COLOR_DIFFUSE 12)

(def ^{:const true} SHADER_LOC_COLOR_SPECULAR 13)

(def ^{:const true} SHADER_LOC_COLOR_AMBIENT 14)

(def ^{:const true} SHADER_LOC_MAP_ALBEDO 15)

(def ^{:const true} SHADER_LOC_MAP_METALNESS 16)

(def ^{:const true} SHADER_LOC_MAP_NORMAL 17)

(def ^{:const true} SHADER_LOC_MAP_ROUGHNESS 18)

(def ^{:const true} SHADER_LOC_MAP_OCCLUSION 19)

(def ^{:const true} SHADER_LOC_MAP_EMISSION 20)

(def ^{:const true} SHADER_LOC_MAP_HEIGHT 21)

(def ^{:const true} SHADER_LOC_MAP_CUBEMAP 22)

(def ^{:const true} SHADER_LOC_MAP_IRRADIANCE 23)

(def ^{:const true} SHADER_LOC_MAP_PREFILTER 24)

(def ^{:const true} SHADER_LOC_MAP_BRDF 25)

(coffi.mem/defalias :raylib/ShaderLocationIndex :coffi.mem/int)

(def ^{:const true} SHADER_UNIFORM_FLOAT 0)

(def ^{:const true} SHADER_UNIFORM_VEC2 1)

(def ^{:const true} SHADER_UNIFORM_VEC3 2)

(def ^{:const true} SHADER_UNIFORM_VEC4 3)

(def ^{:const true} SHADER_UNIFORM_INT 4)

(def ^{:const true} SHADER_UNIFORM_IVEC2 5)

(def ^{:const true} SHADER_UNIFORM_IVEC3 6)

(def ^{:const true} SHADER_UNIFORM_IVEC4 7)

(def ^{:const true} SHADER_UNIFORM_SAMPLER2D 8)

(coffi.mem/defalias :raylib/ShaderUniformDataType :coffi.mem/int)

(def ^{:const true} SHADER_ATTRIB_FLOAT 0)

(def ^{:const true} SHADER_ATTRIB_VEC2 1)

(def ^{:const true} SHADER_ATTRIB_VEC3 2)

(def ^{:const true} SHADER_ATTRIB_VEC4 3)

(coffi.mem/defalias :raylib/ShaderAttributeDataType :coffi.mem/int)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_GRAYSCALE 1)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA 2)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R5G6B5 3)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R8G8B8 4)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R5G5B5A1 5)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R4G4B4A4 6)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R8G8B8A8 7)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R32 8)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R32G32B32 9)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R32G32B32A32 10)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R16 11)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R16G16B16 12)

(def ^{:const true} PIXELFORMAT_UNCOMPRESSED_R16G16B16A16 13)

(def ^{:const true} PIXELFORMAT_COMPRESSED_DXT1_RGB 14)

(def ^{:const true} PIXELFORMAT_COMPRESSED_DXT1_RGBA 15)

(def ^{:const true} PIXELFORMAT_COMPRESSED_DXT3_RGBA 16)

(def ^{:const true} PIXELFORMAT_COMPRESSED_DXT5_RGBA 17)

(def ^{:const true} PIXELFORMAT_COMPRESSED_ETC1_RGB 18)

(def ^{:const true} PIXELFORMAT_COMPRESSED_ETC2_RGB 19)

(def ^{:const true} PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA 20)

(def ^{:const true} PIXELFORMAT_COMPRESSED_PVRT_RGB 21)

(def ^{:const true} PIXELFORMAT_COMPRESSED_PVRT_RGBA 22)

(def ^{:const true} PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA 23)

(def ^{:const true} PIXELFORMAT_COMPRESSED_ASTC_8x8_RGBA 24)

(coffi.mem/defalias :raylib/PixelFormat :coffi.mem/int)

(def ^{:const true} TEXTURE_FILTER_POINT 0)

(def ^{:const true} TEXTURE_FILTER_BILINEAR 1)

(def ^{:const true} TEXTURE_FILTER_TRILINEAR 2)

(def ^{:const true} TEXTURE_FILTER_ANISOTROPIC_4X 3)

(def ^{:const true} TEXTURE_FILTER_ANISOTROPIC_8X 4)

(def ^{:const true} TEXTURE_FILTER_ANISOTROPIC_16X 5)

(coffi.mem/defalias :raylib/TextureFilter :coffi.mem/int)

(def ^{:const true} TEXTURE_WRAP_REPEAT 0)

(def ^{:const true} TEXTURE_WRAP_CLAMP 1)

(def ^{:const true} TEXTURE_WRAP_MIRROR_REPEAT 2)

(def ^{:const true} TEXTURE_WRAP_MIRROR_CLAMP 3)

(coffi.mem/defalias :raylib/TextureWrap :coffi.mem/int)

(def ^{:const true} CUBEMAP_LAYOUT_AUTO_DETECT 0)

(def ^{:const true} CUBEMAP_LAYOUT_LINE_VERTICAL 1)

(def ^{:const true} CUBEMAP_LAYOUT_LINE_HORIZONTAL 2)

(def ^{:const true} CUBEMAP_LAYOUT_CROSS_THREE_BY_FOUR 3)

(def ^{:const true} CUBEMAP_LAYOUT_CROSS_FOUR_BY_THREE 4)

(def ^{:const true} CUBEMAP_LAYOUT_PANORAMA 5)

(coffi.mem/defalias :raylib/CubemapLayout :coffi.mem/int)

(def ^{:const true} FONT_DEFAULT 0)

(def ^{:const true} FONT_BITMAP 1)

(def ^{:const true} FONT_SDF 2)

(coffi.mem/defalias :raylib/FontType :coffi.mem/int)

(def ^{:const true} BLEND_ALPHA 0)

(def ^{:const true} BLEND_ADDITIVE 1)

(def ^{:const true} BLEND_MULTIPLIED 2)

(def ^{:const true} BLEND_ADD_COLORS 3)

(def ^{:const true} BLEND_SUBTRACT_COLORS 4)

(def ^{:const true} BLEND_ALPHA_PREMULTIPLY 5)

(def ^{:const true} BLEND_CUSTOM 6)

(def ^{:const true} BLEND_CUSTOM_SEPARATE 7)

(coffi.mem/defalias :raylib/BlendMode :coffi.mem/int)

(def ^{:const true} GESTURE_NONE 0)

(def ^{:const true} GESTURE_TAP 1)

(def ^{:const true} GESTURE_DOUBLETAP 2)

(def ^{:const true} GESTURE_HOLD 4)

(def ^{:const true} GESTURE_DRAG 8)

(def ^{:const true} GESTURE_SWIPE_RIGHT 16)

(def ^{:const true} GESTURE_SWIPE_LEFT 32)

(def ^{:const true} GESTURE_SWIPE_UP 64)

(def ^{:const true} GESTURE_SWIPE_DOWN 128)

(def ^{:const true} GESTURE_PINCH_IN 256)

(def ^{:const true} GESTURE_PINCH_OUT 512)

(coffi.mem/defalias :raylib/Gesture :coffi.mem/int)

(def ^{:const true} CAMERA_CUSTOM 0)

(def ^{:const true} CAMERA_FREE 1)

(def ^{:const true} CAMERA_ORBITAL 2)

(def ^{:const true} CAMERA_FIRST_PERSON 3)

(def ^{:const true} CAMERA_THIRD_PERSON 4)

(coffi.mem/defalias :raylib/CameraMode :coffi.mem/int)

(def ^{:const true} CAMERA_PERSPECTIVE 0)

(def ^{:const true} CAMERA_ORTHOGRAPHIC 1)

(coffi.mem/defalias :raylib/CameraProjection :coffi.mem/int)

(def ^{:const true} NPATCH_NINE_PATCH 0)

(def ^{:const true} NPATCH_THREE_PATCH_VERTICAL 1)

(def ^{:const true} NPATCH_THREE_PATCH_HORIZONTAL 2)

(coffi.mem/defalias :raylib/NPatchLayout :coffi.mem/int)

(coffi.mem/defalias
 :raylib/TraceLogCallback
 [:coffi.ffi/fn
  [:coffi.mem/int :coffi.mem/c-string :coffi.mem/c-string]
  :coffi.mem/void])

(coffi.mem/defalias
 :raylib/LoadFileDataCallback
 [:coffi.ffi/fn
  [:coffi.mem/c-string [:coffi.mem/pointer :coffi.mem/int]]
  :coffi.mem/c-string])

(coffi.mem/defalias
 :raylib/SaveFileDataCallback
 [:coffi.ffi/fn
  [:coffi.mem/c-string :coffi.mem/pointer :coffi.mem/int]
  :coffimaker.runtime/bool])

(coffi.mem/defalias
 :raylib/LoadFileTextCallback
 [:coffi.ffi/fn [:coffi.mem/c-string] :coffi.mem/c-string])

(coffi.mem/defalias
 :raylib/SaveFileTextCallback
 [:coffi.ffi/fn
  [:coffi.mem/c-string :coffi.mem/c-string]
  :coffimaker.runtime/bool])

(coffi.ffi/defcfn
 InitWindow
 "`int` `int` `string` -> ()"
 {:arglists '([width height title])}
 InitWindow
 [:coffi.mem/int :coffi.mem/int :coffi.mem/c-string]
 :coffi.mem/void)

(coffi.ffi/defcfn
 WindowShouldClose
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 WindowShouldClose
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 CloseWindow
 "() -> ()"
 {:arglists '([])}
 CloseWindow
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsWindowReady
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsWindowReady
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsWindowFullscreen
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsWindowFullscreen
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsWindowHidden
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsWindowHidden
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsWindowMinimized
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsWindowMinimized
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsWindowMaximized
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsWindowMaximized
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsWindowFocused
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsWindowFocused
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsWindowResized
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsWindowResized
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsWindowState
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([flag])}
 IsWindowState
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 SetWindowState
 "`int` -> ()"
 {:arglists '([flags])}
 SetWindowState
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 ClearWindowState
 "`int` -> ()"
 {:arglists '([flags])}
 ClearWindowState
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 ToggleFullscreen
 "() -> ()"
 {:arglists '([])}
 ToggleFullscreen
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 ToggleBorderlessWindowed
 "() -> ()"
 {:arglists '([])}
 ToggleBorderlessWindowed
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 MaximizeWindow
 "() -> ()"
 {:arglists '([])}
 MaximizeWindow
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 MinimizeWindow
 "() -> ()"
 {:arglists '([])}
 MinimizeWindow
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 RestoreWindow
 "() -> ()"
 {:arglists '([])}
 RestoreWindow
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetWindowIcon
 "`raylib/Image` -> ()"
 {:arglists '([image])}
 SetWindowIcon
 [:raylib/Image]
 :coffi.mem/void)

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  alloc-raylib__Image-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc (clojure.core/* len size-of-raylib__Image) arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  SetWindowIcons
  "[`raylib/Image`] -> ()"
  {:arglists '([images])}
  SetWindowIcons
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void
  SetWindowIcons-native
  [images]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [count
     (clojure.core/count images)
     images'
     (clojure.core/let
      [local-segment (alloc-raylib__Image-list count arena)]
      (clojure.core/loop
       [xs (clojure.core/seq images) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__Image
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__Image)))))
      local-segment)
     return-value-raw
     (SetWindowIcons-native images' count)
     return-value
     return-value-raw]
    nil))))

(coffi.ffi/defcfn
 SetWindowTitle
 "`string` -> ()"
 {:arglists '([title])}
 SetWindowTitle
 [:coffi.mem/c-string]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetWindowPosition
 "`int` `int` -> ()"
 {:arglists '([x y])}
 SetWindowPosition
 [:coffi.mem/int :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetWindowMonitor
 "`int` -> ()"
 {:arglists '([monitor])}
 SetWindowMonitor
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetWindowMinSize
 "`int` `int` -> ()"
 {:arglists '([width height])}
 SetWindowMinSize
 [:coffi.mem/int :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetWindowSize
 "`int` `int` -> ()"
 {:arglists '([width height])}
 SetWindowSize
 [:coffi.mem/int :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetWindowOpacity
 "`float` -> ()"
 {:arglists '([opacity])}
 SetWindowOpacity
 [:coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetWindowFocused
 "() -> ()"
 {:arglists '([])}
 SetWindowFocused
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetWindowHandle
 "() -> `pointer`"
 {:arglists '([])}
 GetWindowHandle
 []
 :coffi.mem/pointer)

(coffi.ffi/defcfn
 GetScreenWidth
 "() -> `int`"
 {:arglists '([])}
 GetScreenWidth
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetScreenHeight
 "() -> `int`"
 {:arglists '([])}
 GetScreenHeight
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetRenderWidth
 "() -> `int`"
 {:arglists '([])}
 GetRenderWidth
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetRenderHeight
 "() -> `int`"
 {:arglists '([])}
 GetRenderHeight
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetMonitorCount
 "() -> `int`"
 {:arglists '([])}
 GetMonitorCount
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetCurrentMonitor
 "() -> `int`"
 {:arglists '([])}
 GetCurrentMonitor
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetMonitorPosition
 "`int` -> `raylib/Vector2`"
 {:arglists '([monitor])}
 GetMonitorPosition
 [:coffi.mem/int]
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetMonitorWidth
 "`int` -> `int`"
 {:arglists '([monitor])}
 GetMonitorWidth
 [:coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetMonitorHeight
 "`int` -> `int`"
 {:arglists '([monitor])}
 GetMonitorHeight
 [:coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetMonitorPhysicalWidth
 "`int` -> `int`"
 {:arglists '([monitor])}
 GetMonitorPhysicalWidth
 [:coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetMonitorPhysicalHeight
 "`int` -> `int`"
 {:arglists '([monitor])}
 GetMonitorPhysicalHeight
 [:coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetMonitorRefreshRate
 "`int` -> `int`"
 {:arglists '([monitor])}
 GetMonitorRefreshRate
 [:coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetWindowPosition
 "() -> `raylib/Vector2`"
 {:arglists '([])}
 GetWindowPosition
 []
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetWindowScaleDPI
 "() -> `raylib/Vector2`"
 {:arglists '([])}
 GetWindowScaleDPI
 []
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetMonitorName
 "`int` -> `string`"
 {:arglists '([monitor])}
 GetMonitorName
 [:coffi.mem/int]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 SetClipboardText
 "`string` -> ()"
 {:arglists '([text])}
 SetClipboardText
 [:coffi.mem/c-string]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetClipboardText
 "() -> `string`"
 {:arglists '([])}
 GetClipboardText
 []
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 EnableEventWaiting
 "() -> ()"
 {:arglists '([])}
 EnableEventWaiting
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 DisableEventWaiting
 "() -> ()"
 {:arglists '([])}
 DisableEventWaiting
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 SwapScreenBuffer
 "() -> ()"
 {:arglists '([])}
 SwapScreenBuffer
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 PollInputEvents
 "() -> ()"
 {:arglists '([])}
 PollInputEvents
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 WaitTime
 "`double` -> ()"
 {:arglists '([seconds])}
 WaitTime
 [:coffi.mem/double]
 :coffi.mem/void)

(coffi.ffi/defcfn
 ShowCursor
 "() -> ()"
 {:arglists '([])}
 ShowCursor
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 HideCursor
 "() -> ()"
 {:arglists '([])}
 HideCursor
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsCursorHidden
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsCursorHidden
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 EnableCursor
 "() -> ()"
 {:arglists '([])}
 EnableCursor
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 DisableCursor
 "() -> ()"
 {:arglists '([])}
 DisableCursor
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsCursorOnScreen
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsCursorOnScreen
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 ClearBackground
 "`raylib/Color` -> ()"
 {:arglists '([color])}
 ClearBackground
 [:raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 BeginDrawing
 "() -> ()"
 {:arglists '([])}
 BeginDrawing
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 EndDrawing
 "() -> ()"
 {:arglists '([])}
 EndDrawing
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 BeginMode2D
 "`raylib/Camera2D` -> ()"
 {:arglists '([camera])}
 BeginMode2D
 [:raylib/Camera2D]
 :coffi.mem/void)

(coffi.ffi/defcfn
 EndMode2D
 "() -> ()"
 {:arglists '([])}
 EndMode2D
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 BeginMode3D
 "`raylib/Camera3D` -> ()"
 {:arglists '([camera])}
 BeginMode3D
 [:raylib/Camera3D]
 :coffi.mem/void)

(coffi.ffi/defcfn
 EndMode3D
 "() -> ()"
 {:arglists '([])}
 EndMode3D
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 BeginTextureMode
 "`raylib/RenderTexture` -> ()"
 {:arglists '([target])}
 BeginTextureMode
 [:raylib/RenderTexture]
 :coffi.mem/void)

(coffi.ffi/defcfn
 EndTextureMode
 "() -> ()"
 {:arglists '([])}
 EndTextureMode
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 BeginShaderMode
 "`raylib/Shader` -> ()"
 {:arglists '([shader])}
 BeginShaderMode
 [:raylib/Shader]
 :coffi.mem/void)

(coffi.ffi/defcfn
 EndShaderMode
 "() -> ()"
 {:arglists '([])}
 EndShaderMode
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 BeginBlendMode
 "`int` -> ()"
 {:arglists '([mode])}
 BeginBlendMode
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 EndBlendMode
 "() -> ()"
 {:arglists '([])}
 EndBlendMode
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 BeginScissorMode
 "`int` `int` `int` `int` -> ()"
 {:arglists '([x y width height])}
 BeginScissorMode
 [:coffi.mem/int :coffi.mem/int :coffi.mem/int :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 EndScissorMode
 "() -> ()"
 {:arglists '([])}
 EndScissorMode
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 BeginVrStereoMode
 "`raylib/VrStereoConfig` -> ()"
 {:arglists '([config])}
 BeginVrStereoMode
 [:raylib/VrStereoConfig]
 :coffi.mem/void)

(coffi.ffi/defcfn
 EndVrStereoMode
 "() -> ()"
 {:arglists '([])}
 EndVrStereoMode
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 LoadVrStereoConfig
 "`raylib/VrDeviceInfo` -> `raylib/VrStereoConfig`"
 {:arglists '([device])}
 LoadVrStereoConfig
 [:raylib/VrDeviceInfo]
 :raylib/VrStereoConfig)

(coffi.ffi/defcfn
 UnloadVrStereoConfig
 "`raylib/VrStereoConfig` -> ()"
 {:arglists '([config])}
 UnloadVrStereoConfig
 [:raylib/VrStereoConfig]
 :coffi.mem/void)

(coffi.ffi/defcfn
 LoadShader
 "`string` `string` -> `raylib/Shader`"
 {:arglists '([vsFileName fsFileName])}
 LoadShader
 [:coffi.mem/c-string :coffi.mem/c-string]
 :raylib/Shader)

(coffi.ffi/defcfn
 LoadShaderFromMemory
 "`string` `string` -> `raylib/Shader`"
 {:arglists '([vsCode fsCode])}
 LoadShaderFromMemory
 [:coffi.mem/c-string :coffi.mem/c-string]
 :raylib/Shader)

(coffi.ffi/defcfn
 IsShaderReady
 "`raylib/Shader` -> `coffimaker.runtime/bool`"
 {:arglists '([shader])}
 IsShaderReady
 [:raylib/Shader]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GetShaderLocation
 "`raylib/Shader` `string` -> `int`"
 {:arglists '([shader uniformName])}
 GetShaderLocation
 [:raylib/Shader :coffi.mem/c-string]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetShaderLocationAttrib
 "`raylib/Shader` `string` -> `int`"
 {:arglists '([shader attribName])}
 GetShaderLocationAttrib
 [:raylib/Shader :coffi.mem/c-string]
 :coffi.mem/int)

(coffi.ffi/defcfn
 SetShaderValue
 "`raylib/Shader` `int` `pointer` `int` -> ()"
 {:arglists '([shader locIndex value uniformType])}
 SetShaderValue
 [:raylib/Shader :coffi.mem/int :coffi.mem/pointer :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetShaderValueV
 "`raylib/Shader` `int` `pointer` `int` `int` -> ()"
 {:arglists '([shader locIndex value uniformType count])}
 SetShaderValueV
 [:raylib/Shader
  :coffi.mem/int
  :coffi.mem/pointer
  :coffi.mem/int
  :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetShaderValueMatrix
 "`raylib/Shader` `int` `raylib/Matrix` -> ()"
 {:arglists '([shader locIndex mat])}
 SetShaderValueMatrix
 [:raylib/Shader :coffi.mem/int :raylib/Matrix]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetShaderValueTexture
 "`raylib/Shader` `int` `raylib/Texture` -> ()"
 {:arglists '([shader locIndex texture])}
 SetShaderValueTexture
 [:raylib/Shader :coffi.mem/int :raylib/Texture]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UnloadShader
 "`raylib/Shader` -> ()"
 {:arglists '([shader])}
 UnloadShader
 [:raylib/Shader]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetMouseRay
 "`raylib/Vector2` `raylib/Camera3D` -> `raylib/Ray`"
 {:arglists '([mousePosition camera])}
 GetMouseRay
 [:raylib/Vector2 :raylib/Camera3D]
 :raylib/Ray)

(coffi.ffi/defcfn
 GetCameraMatrix
 "`raylib/Camera3D` -> `raylib/Matrix`"
 {:arglists '([camera])}
 GetCameraMatrix
 [:raylib/Camera3D]
 :raylib/Matrix)

(coffi.ffi/defcfn
 GetCameraMatrix2D
 "`raylib/Camera2D` -> `raylib/Matrix`"
 {:arglists '([camera])}
 GetCameraMatrix2D
 [:raylib/Camera2D]
 :raylib/Matrix)

(coffi.ffi/defcfn
 GetWorldToScreen
 "`raylib/Vector3` `raylib/Camera3D` -> `raylib/Vector2`"
 {:arglists '([position camera])}
 GetWorldToScreen
 [:raylib/Vector3 :raylib/Camera3D]
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetScreenToWorld2D
 "`raylib/Vector2` `raylib/Camera2D` -> `raylib/Vector2`"
 {:arglists '([position camera])}
 GetScreenToWorld2D
 [:raylib/Vector2 :raylib/Camera2D]
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetWorldToScreenEx
 "`raylib/Vector3` `raylib/Camera3D` `int` `int` -> `raylib/Vector2`"
 {:arglists '([position camera width height])}
 GetWorldToScreenEx
 [:raylib/Vector3 :raylib/Camera3D :coffi.mem/int :coffi.mem/int]
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetWorldToScreen2D
 "`raylib/Vector2` `raylib/Camera2D` -> `raylib/Vector2`"
 {:arglists '([position camera])}
 GetWorldToScreen2D
 [:raylib/Vector2 :raylib/Camera2D]
 :raylib/Vector2)

(coffi.ffi/defcfn
 SetTargetFPS
 "`int` -> ()"
 {:arglists '([fps])}
 SetTargetFPS
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetFPS
 "() -> `int`"
 {:arglists '([])}
 GetFPS
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetFrameTime
 "() -> `float`"
 {:arglists '([])}
 GetFrameTime
 []
 :coffi.mem/float)

(coffi.ffi/defcfn
 GetTime
 "() -> `double`"
 {:arglists '([])}
 GetTime
 []
 :coffi.mem/double)

(coffi.ffi/defcfn
 GetRandomValue
 "`int` `int` -> `int`"
 {:arglists '([min max])}
 GetRandomValue
 [:coffi.mem/int :coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 SetRandomSeed
 "`int` -> ()"
 {:arglists '([seed])}
 SetRandomSeed
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 TakeScreenshot
 "`string` -> ()"
 {:arglists '([fileName])}
 TakeScreenshot
 [:coffi.mem/c-string]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetConfigFlags
 "`int` -> ()"
 {:arglists '([flags])}
 SetConfigFlags
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 TraceLog
 "`int` `string` -> ()"
 {:arglists '([logLevel text])}
 TraceLog
 [:coffi.mem/int :coffi.mem/c-string]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetTraceLogLevel
 "`int` -> ()"
 {:arglists '([logLevel])}
 SetTraceLogLevel
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 MemAlloc
 "`int` -> `pointer`"
 {:arglists '([size])}
 MemAlloc
 [:coffi.mem/int]
 :coffi.mem/pointer)

(coffi.ffi/defcfn
 MemRealloc
 "`pointer` `int` -> `pointer`"
 {:arglists '([ptr size])}
 MemRealloc
 [:coffi.mem/pointer :coffi.mem/int]
 :coffi.mem/pointer)

(coffi.ffi/defcfn
 MemFree
 "`pointer` -> ()"
 {:arglists '([ptr])}
 MemFree
 [:coffi.mem/pointer]
 :coffi.mem/void)

(coffi.ffi/defcfn
 OpenURL
 "`string` -> ()"
 {:arglists '([url])}
 OpenURL
 [:coffi.mem/c-string]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetTraceLogCallback
 "(`int` `string` `string` -> ()) -> ()"
 {:arglists '([callback])}
 SetTraceLogCallback
 [[:coffi.ffi/fn
   [:coffi.mem/int :coffi.mem/c-string :coffi.mem/c-string]
   :coffi.mem/void]]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetLoadFileDataCallback
 "(`string` *`int` -> `string`) -> ()"
 {:arglists '([callback])}
 SetLoadFileDataCallback
 [[:coffi.ffi/fn
   [:coffi.mem/c-string [:coffi.mem/pointer :coffi.mem/int]]
   :coffi.mem/c-string]]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetSaveFileDataCallback
 "(`string` `pointer` `int` -> `coffimaker.runtime/bool`) -> ()"
 {:arglists '([callback])}
 SetSaveFileDataCallback
 [[:coffi.ffi/fn
   [:coffi.mem/c-string :coffi.mem/pointer :coffi.mem/int]
   :coffimaker.runtime/bool]]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetLoadFileTextCallback
 "(`string` -> `string`) -> ()"
 {:arglists '([callback])}
 SetLoadFileTextCallback
 [[:coffi.ffi/fn [:coffi.mem/c-string] :coffi.mem/c-string]]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetSaveFileTextCallback
 "(`string` `string` -> `coffimaker.runtime/bool`) -> ()"
 {:arglists '([callback])}
 SetSaveFileTextCallback
 [[:coffi.ffi/fn
   [:coffi.mem/c-string :coffi.mem/c-string]
   :coffimaker.runtime/bool]]
 :coffi.mem/void)

(clojure.core/let
 [size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  size-of-coffi_mem__byte
  (coffi.mem/size-of :coffi.mem/byte)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  deserialize-from-coffi_mem__byte
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/byte)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))]
 (coffi.ffi/defcfn
  LoadFileData
  "`string` -> [`u8`]"
  {:arglists '([fileName])}
  LoadFileData
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/pointer
  LoadFileData-native
  [fileName]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [bytesRead'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (LoadFileData-native fileName bytesRead')
     bytesRead
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment bytesRead' 4)
      0)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i bytesRead)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (coffi.mem/read-byte
          (.reinterpret
           ^java.lang.foreign.MemorySegment return-value-raw
           (clojure.core/* bytesRead size-of-coffi_mem__byte))
          (clojure.core/* 1 i))))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(coffi.ffi/defcfn
 UnloadFileData
 "*`byte` -> ()"
 {:arglists '([data])}
 UnloadFileData
 [:coffi.mem/pointer]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SaveFileData
 "`string` `pointer` `int` -> `coffimaker.runtime/bool`"
 {:arglists '([fileName data bytesToWrite])}
 SaveFileData
 [:coffi.mem/c-string :coffi.mem/pointer :coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 ExportDataAsCode
 "`string` `int` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([data size fileName])}
 ExportDataAsCode
 [:coffi.mem/c-string :coffi.mem/int :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 LoadFileText
 "`string` -> `string`"
 {:arglists '([fileName])}
 LoadFileText
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 UnloadFileText
 "`string` -> ()"
 {:arglists '([text])}
 UnloadFileText
 [:coffi.mem/c-string]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SaveFileText
 "`string` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([fileName text])}
 SaveFileText
 [:coffi.mem/c-string :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 FileExists
 "`string` -> `coffimaker.runtime/bool`"
 {:arglists '([fileName])}
 FileExists
 [:coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 DirectoryExists
 "`string` -> `coffimaker.runtime/bool`"
 {:arglists '([dirPath])}
 DirectoryExists
 [:coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsFileExtension
 "`string` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([fileName ext])}
 IsFileExtension
 [:coffi.mem/c-string :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GetFileLength
 "`string` -> `int`"
 {:arglists '([fileName])}
 GetFileLength
 [:coffi.mem/c-string]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetFileExtension
 "`string` -> `string`"
 {:arglists '([fileName])}
 GetFileExtension
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 GetFileName
 "`string` -> `string`"
 {:arglists '([filePath])}
 GetFileName
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 GetFileNameWithoutExt
 "`string` -> `string`"
 {:arglists '([filePath])}
 GetFileNameWithoutExt
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 GetDirectoryPath
 "`string` -> `string`"
 {:arglists '([filePath])}
 GetDirectoryPath
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 GetPrevDirectoryPath
 "`string` -> `string`"
 {:arglists '([dirPath])}
 GetPrevDirectoryPath
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 GetWorkingDirectory
 "() -> `string`"
 {:arglists '([])}
 GetWorkingDirectory
 []
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 GetApplicationDirectory
 "() -> `string`"
 {:arglists '([])}
 GetApplicationDirectory
 []
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 ChangeDirectory
 "`string` -> `coffimaker.runtime/bool`"
 {:arglists '([dir])}
 ChangeDirectory
 [:coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsPathFile
 "`string` -> `coffimaker.runtime/bool`"
 {:arglists '([path])}
 IsPathFile
 [:coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 LoadDirectoryFiles
 "`string` -> `raylib/FilePathList`"
 {:arglists '([dirPath])}
 LoadDirectoryFiles
 [:coffi.mem/c-string]
 :raylib/FilePathList)

(coffi.ffi/defcfn
 LoadDirectoryFilesEx
 "`string` `string` `coffimaker.runtime/bool` -> `raylib/FilePathList`"
 {:arglists '([basePath filter scanSubdirs])}
 LoadDirectoryFilesEx
 [:coffi.mem/c-string :coffi.mem/c-string :coffimaker.runtime/bool]
 :raylib/FilePathList)

(coffi.ffi/defcfn
 UnloadDirectoryFiles
 "`raylib/FilePathList` -> ()"
 {:arglists '([files])}
 UnloadDirectoryFiles
 [:raylib/FilePathList]
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsFileDropped
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsFileDropped
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 LoadDroppedFiles
 "() -> `raylib/FilePathList`"
 {:arglists '([])}
 LoadDroppedFiles
 []
 :raylib/FilePathList)

(coffi.ffi/defcfn
 UnloadDroppedFiles
 "`raylib/FilePathList` -> ()"
 {:arglists '([files])}
 UnloadDroppedFiles
 [:raylib/FilePathList]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetFileModTime
 "`string` -> `int`"
 {:arglists '([fileName])}
 GetFileModTime
 [:coffi.mem/c-string]
 :coffi.mem/int)

(clojure.core/let
 [size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  size-of-coffi_mem__byte
  (coffi.mem/size-of :coffi.mem/byte)
  serialize-into-coffi_mem__byte
  (clojure.core/get-method coffi.mem/serialize-into :coffi.mem/byte)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  deserialize-from-coffi_mem__byte
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/byte)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))
  alloc-coffi_mem__byte-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-coffi_mem__byte)
    arena))]
 (coffi.ffi/defcfn
  CompressData
  "[`u8`] -> [`u8`]"
  {:arglists '([data])}
  CompressData
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/pointer]
  :coffi.mem/pointer
  CompressData-native
  [data]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dataSize
     (clojure.core/count data)
     data'
     (clojure.core/let
      [local-segment (alloc-coffi_mem__byte-list dataSize arena)]
      (clojure.core/loop
       [xs (clojure.core/seq data) offset 0]
       (if
        xs
        (do
         (coffi.mem/write-byte
          local-segment
          offset
          (clojure.core/first xs))
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-coffi_mem__byte)))))
      local-segment)
     compDataSize'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (CompressData-native data' dataSize compDataSize')
     compDataSize
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment compDataSize' 4)
      0)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i compDataSize)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (coffi.mem/read-byte
          (.reinterpret
           ^java.lang.foreign.MemorySegment return-value-raw
           (clojure.core/* compDataSize size-of-coffi_mem__byte))
          (clojure.core/* 1 i))))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(clojure.core/let
 [size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  size-of-coffi_mem__byte
  (coffi.mem/size-of :coffi.mem/byte)
  serialize-into-coffi_mem__byte
  (clojure.core/get-method coffi.mem/serialize-into :coffi.mem/byte)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  deserialize-from-coffi_mem__byte
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/byte)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))
  alloc-coffi_mem__byte-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-coffi_mem__byte)
    arena))]
 (coffi.ffi/defcfn
  DecompressData
  "[`u8`] -> [`u8`]"
  {:arglists '([compData])}
  DecompressData
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/pointer]
  :coffi.mem/pointer
  DecompressData-native
  [compData]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [compDataSize
     (clojure.core/count compData)
     compData'
     (clojure.core/let
      [local-segment (alloc-coffi_mem__byte-list compDataSize arena)]
      (clojure.core/loop
       [xs (clojure.core/seq compData) offset 0]
       (if
        xs
        (do
         (coffi.mem/write-byte
          local-segment
          offset
          (clojure.core/first xs))
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-coffi_mem__byte)))))
      local-segment)
     dataSize'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (DecompressData-native compData' compDataSize dataSize')
     dataSize
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment dataSize' 4)
      0)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i dataSize)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (coffi.mem/read-byte
          (.reinterpret
           ^java.lang.foreign.MemorySegment return-value-raw
           (clojure.core/* dataSize size-of-coffi_mem__byte))
          (clojure.core/* 1 i))))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(clojure.core/let
 [size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  size-of-coffi_mem__byte
  (coffi.mem/size-of :coffi.mem/byte)
  serialize-into-coffi_mem__byte
  (clojure.core/get-method coffi.mem/serialize-into :coffi.mem/byte)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  deserialize-from-coffi_mem__byte
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/byte)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))
  alloc-coffi_mem__byte-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-coffi_mem__byte)
    arena))]
 (coffi.ffi/defcfn
  EncodeDataBase64
  "[`u8`] -> [`u8`]"
  {:arglists '([data])}
  EncodeDataBase64
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/pointer]
  :coffi.mem/pointer
  EncodeDataBase64-native
  [data]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dataSize
     (clojure.core/count data)
     data'
     (clojure.core/let
      [local-segment (alloc-coffi_mem__byte-list dataSize arena)]
      (clojure.core/loop
       [xs (clojure.core/seq data) offset 0]
       (if
        xs
        (do
         (coffi.mem/write-byte
          local-segment
          offset
          (clojure.core/first xs))
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-coffi_mem__byte)))))
      local-segment)
     outputSize'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (EncodeDataBase64-native data' dataSize outputSize')
     outputSize
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment outputSize' 4)
      0)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i outputSize)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (coffi.mem/read-byte
          (.reinterpret
           ^java.lang.foreign.MemorySegment return-value-raw
           (clojure.core/* outputSize size-of-coffi_mem__byte))
          (clojure.core/* 1 i))))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(clojure.core/let
 [size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  size-of-coffi_mem__byte
  (coffi.mem/size-of :coffi.mem/byte)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  deserialize-from-coffi_mem__byte
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/byte)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))]
 (coffi.ffi/defcfn
  DecodeDataBase64
  "`string` -> [`u8`]"
  {:arglists '([data])}
  DecodeDataBase64
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/pointer
  DecodeDataBase64-native
  [data]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [outputSize'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (DecodeDataBase64-native data outputSize')
     outputSize
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment outputSize' 4)
      0)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i outputSize)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (coffi.mem/read-byte
          (.reinterpret
           ^java.lang.foreign.MemorySegment return-value-raw
           (clojure.core/* outputSize size-of-coffi_mem__byte))
          (clojure.core/* 1 i))))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(coffi.ffi/defcfn
 IsKeyPressed
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([key])}
 IsKeyPressed
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsKeyPressedRepeat
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([key])}
 IsKeyPressedRepeat
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsKeyDown
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([key])}
 IsKeyDown
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsKeyReleased
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([key])}
 IsKeyReleased
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsKeyUp
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([key])}
 IsKeyUp
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 SetExitKey
 "`int` -> ()"
 {:arglists '([key])}
 SetExitKey
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetKeyPressed
 "() -> `int`"
 {:arglists '([])}
 GetKeyPressed
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetCharPressed
 "() -> `int`"
 {:arglists '([])}
 GetCharPressed
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 IsGamepadAvailable
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([gamepad])}
 IsGamepadAvailable
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GetGamepadName
 "`int` -> `string`"
 {:arglists '([gamepad])}
 GetGamepadName
 [:coffi.mem/int]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 IsGamepadButtonPressed
 "`int` `int` -> `coffimaker.runtime/bool`"
 {:arglists '([gamepad button])}
 IsGamepadButtonPressed
 [:coffi.mem/int :coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsGamepadButtonDown
 "`int` `int` -> `coffimaker.runtime/bool`"
 {:arglists '([gamepad button])}
 IsGamepadButtonDown
 [:coffi.mem/int :coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsGamepadButtonReleased
 "`int` `int` -> `coffimaker.runtime/bool`"
 {:arglists '([gamepad button])}
 IsGamepadButtonReleased
 [:coffi.mem/int :coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsGamepadButtonUp
 "`int` `int` -> `coffimaker.runtime/bool`"
 {:arglists '([gamepad button])}
 IsGamepadButtonUp
 [:coffi.mem/int :coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GetGamepadButtonPressed
 "() -> `int`"
 {:arglists '([])}
 GetGamepadButtonPressed
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetGamepadAxisCount
 "`int` -> `int`"
 {:arglists '([gamepad])}
 GetGamepadAxisCount
 [:coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetGamepadAxisMovement
 "`int` `int` -> `float`"
 {:arglists '([gamepad axis])}
 GetGamepadAxisMovement
 [:coffi.mem/int :coffi.mem/int]
 :coffi.mem/float)

(coffi.ffi/defcfn
 SetGamepadMappings
 "`string` -> `int`"
 {:arglists '([mappings])}
 SetGamepadMappings
 [:coffi.mem/c-string]
 :coffi.mem/int)

(coffi.ffi/defcfn
 IsMouseButtonPressed
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([button])}
 IsMouseButtonPressed
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsMouseButtonDown
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([button])}
 IsMouseButtonDown
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsMouseButtonReleased
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([button])}
 IsMouseButtonReleased
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 IsMouseButtonUp
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([button])}
 IsMouseButtonUp
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GetMouseX
 "() -> `int`"
 {:arglists '([])}
 GetMouseX
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetMouseY
 "() -> `int`"
 {:arglists '([])}
 GetMouseY
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetMousePosition
 "() -> `raylib/Vector2`"
 {:arglists '([])}
 GetMousePosition
 []
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetMouseDelta
 "() -> `raylib/Vector2`"
 {:arglists '([])}
 GetMouseDelta
 []
 :raylib/Vector2)

(coffi.ffi/defcfn
 SetMousePosition
 "`int` `int` -> ()"
 {:arglists '([x y])}
 SetMousePosition
 [:coffi.mem/int :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetMouseOffset
 "`int` `int` -> ()"
 {:arglists '([offsetX offsetY])}
 SetMouseOffset
 [:coffi.mem/int :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetMouseScale
 "`float` `float` -> ()"
 {:arglists '([scaleX scaleY])}
 SetMouseScale
 [:coffi.mem/float :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetMouseWheelMove
 "() -> `float`"
 {:arglists '([])}
 GetMouseWheelMove
 []
 :coffi.mem/float)

(coffi.ffi/defcfn
 GetMouseWheelMoveV
 "() -> `raylib/Vector2`"
 {:arglists '([])}
 GetMouseWheelMoveV
 []
 :raylib/Vector2)

(coffi.ffi/defcfn
 SetMouseCursor
 "`int` -> ()"
 {:arglists '([cursor])}
 SetMouseCursor
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetTouchX
 "() -> `int`"
 {:arglists '([])}
 GetTouchX
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetTouchY
 "() -> `int`"
 {:arglists '([])}
 GetTouchY
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetTouchPosition
 "`int` -> `raylib/Vector2`"
 {:arglists '([index])}
 GetTouchPosition
 [:coffi.mem/int]
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetTouchPointId
 "`int` -> `int`"
 {:arglists '([index])}
 GetTouchPointId
 [:coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetTouchPointCount
 "() -> `int`"
 {:arglists '([])}
 GetTouchPointCount
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 SetGesturesEnabled
 "`int` -> ()"
 {:arglists '([flags])}
 SetGesturesEnabled
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsGestureDetected
 "`int` -> `coffimaker.runtime/bool`"
 {:arglists '([gesture])}
 IsGestureDetected
 [:coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GetGestureDetected
 "() -> `int`"
 {:arglists '([])}
 GetGestureDetected
 []
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetGestureHoldDuration
 "() -> `float`"
 {:arglists '([])}
 GetGestureHoldDuration
 []
 :coffi.mem/float)

(coffi.ffi/defcfn
 GetGestureDragVector
 "() -> `raylib/Vector2`"
 {:arglists '([])}
 GetGestureDragVector
 []
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetGestureDragAngle
 "() -> `float`"
 {:arglists '([])}
 GetGestureDragAngle
 []
 :coffi.mem/float)

(coffi.ffi/defcfn
 GetGesturePinchVector
 "() -> `raylib/Vector2`"
 {:arglists '([])}
 GetGesturePinchVector
 []
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetGesturePinchAngle
 "() -> `float`"
 {:arglists '([])}
 GetGesturePinchAngle
 []
 :coffi.mem/float)

(coffi.ffi/defcfn
 UpdateCamera
 "*`raylib/Camera3D` `int` -> ()"
 {:arglists '([camera mode])}
 UpdateCamera
 [:coffi.mem/pointer :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UpdateCameraPro
 "*`raylib/Camera3D` `raylib/Vector3` `raylib/Vector3` `float` -> ()"
 {:arglists '([camera movement rotation zoom])}
 UpdateCameraPro
 [:coffi.mem/pointer :raylib/Vector3 :raylib/Vector3 :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetShapesTexture
 "`raylib/Texture` `raylib/Rectangle` -> ()"
 {:arglists '([texture source])}
 SetShapesTexture
 [:raylib/Texture :raylib/Rectangle]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawPixel
 "`int` `int` `raylib/Color` -> ()"
 {:arglists '([posX posY color])}
 DrawPixel
 [:coffi.mem/int :coffi.mem/int :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawPixelV
 "`raylib/Vector2` `raylib/Color` -> ()"
 {:arglists '([position color])}
 DrawPixelV
 [:raylib/Vector2 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawLine
 "`int` `int` `int` `int` `raylib/Color` -> ()"
 {:arglists '([startPosX startPosY endPosX endPosY color])}
 DrawLine
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawLineV
 "`raylib/Vector2` `raylib/Vector2` `raylib/Color` -> ()"
 {:arglists '([startPos endPos color])}
 DrawLineV
 [:raylib/Vector2 :raylib/Vector2 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawLineEx
 "`raylib/Vector2` `raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists '([startPos endPos thick color])}
 DrawLineEx
 [:raylib/Vector2 :raylib/Vector2 :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawLineBezier
 "`raylib/Vector2` `raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists '([startPos endPos thick color])}
 DrawLineBezier
 [:raylib/Vector2 :raylib/Vector2 :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawLineBezierQuad
 "`raylib/Vector2` `raylib/Vector2` `raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists '([startPos endPos controlPos thick color])}
 DrawLineBezierQuad
 [:raylib/Vector2
  :raylib/Vector2
  :raylib/Vector2
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawLineBezierCubic
 "`raylib/Vector2` `raylib/Vector2` `raylib/Vector2` `raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists
  '([startPos endPos startControlPos endControlPos thick color])}
 DrawLineBezierCubic
 [:raylib/Vector2
  :raylib/Vector2
  :raylib/Vector2
  :raylib/Vector2
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(clojure.core/let
 [size-of-raylib__Vector2
  (coffi.mem/size-of :raylib/Vector2)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Vector2
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Vector2)
  alloc-raylib__Vector2-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-raylib__Vector2)
    arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  DrawLineBSpline
  "[`raylib/Vector2`] `float` `raylib/Color` -> ()"
  {:arglists '([points thick color])}
  DrawLineBSpline
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/float :raylib/Color]
  :coffi.mem/void
  DrawLineBSpline-native
  [points thick color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [pointCount
     (clojure.core/count points)
     points'
     (clojure.core/let
      [local-segment (alloc-raylib__Vector2-list pointCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq points) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__Vector2
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__Vector2)))))
      local-segment)
     return-value-raw
     (DrawLineBSpline-native points' pointCount thick color)
     return-value
     return-value-raw]
    nil))))

(clojure.core/let
 [size-of-raylib__Vector2
  (coffi.mem/size-of :raylib/Vector2)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Vector2
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Vector2)
  alloc-raylib__Vector2-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-raylib__Vector2)
    arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  DrawLineCatmullRom
  "[`raylib/Vector2`] `float` `raylib/Color` -> ()"
  {:arglists '([points thick color])}
  DrawLineCatmullRom
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/float :raylib/Color]
  :coffi.mem/void
  DrawLineCatmullRom-native
  [points thick color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [pointCount
     (clojure.core/count points)
     points'
     (clojure.core/let
      [local-segment (alloc-raylib__Vector2-list pointCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq points) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__Vector2
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__Vector2)))))
      local-segment)
     return-value-raw
     (DrawLineCatmullRom-native points' pointCount thick color)
     return-value
     return-value-raw]
    nil))))

(clojure.core/let
 [size-of-raylib__Vector2
  (coffi.mem/size-of :raylib/Vector2)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Vector2
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Vector2)
  alloc-raylib__Vector2-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-raylib__Vector2)
    arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  DrawLineStrip
  "[`raylib/Vector2`] `raylib/Color` -> ()"
  {:arglists '([points color])}
  DrawLineStrip
  [:coffi.mem/pointer :coffi.mem/int :raylib/Color]
  :coffi.mem/void
  DrawLineStrip-native
  [points color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [pointCount
     (clojure.core/count points)
     points'
     (clojure.core/let
      [local-segment (alloc-raylib__Vector2-list pointCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq points) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__Vector2
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__Vector2)))))
      local-segment)
     return-value-raw
     (DrawLineStrip-native points' pointCount color)
     return-value
     return-value-raw]
    nil))))

(coffi.ffi/defcfn
 DrawCircle
 "`int` `int` `float` `raylib/Color` -> ()"
 {:arglists '([centerX centerY radius color])}
 DrawCircle
 [:coffi.mem/int :coffi.mem/int :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCircleSector
 "`raylib/Vector2` `float` `float` `float` `int` `raylib/Color` -> ()"
 {:arglists '([center radius startAngle endAngle segments color])}
 DrawCircleSector
 [:raylib/Vector2
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCircleSectorLines
 "`raylib/Vector2` `float` `float` `float` `int` `raylib/Color` -> ()"
 {:arglists '([center radius startAngle endAngle segments color])}
 DrawCircleSectorLines
 [:raylib/Vector2
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCircleGradient
 "`int` `int` `float` `raylib/Color` `raylib/Color` -> ()"
 {:arglists '([centerX centerY radius color1 color2])}
 DrawCircleGradient
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/float
  :raylib/Color
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCircleV
 "`raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists '([center radius color])}
 DrawCircleV
 [:raylib/Vector2 :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCircleLines
 "`int` `int` `float` `raylib/Color` -> ()"
 {:arglists '([centerX centerY radius color])}
 DrawCircleLines
 [:coffi.mem/int :coffi.mem/int :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawEllipse
 "`int` `int` `float` `float` `raylib/Color` -> ()"
 {:arglists '([centerX centerY radiusH radiusV color])}
 DrawEllipse
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawEllipseLines
 "`int` `int` `float` `float` `raylib/Color` -> ()"
 {:arglists '([centerX centerY radiusH radiusV color])}
 DrawEllipseLines
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRing
 "`raylib/Vector2` `float` `float` `float` `float` `int` `raylib/Color` -> ()"
 {:arglists
  '([center
     innerRadius
     outerRadius
     startAngle
     endAngle
     segments
     color])}
 DrawRing
 [:raylib/Vector2
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRingLines
 "`raylib/Vector2` `float` `float` `float` `float` `int` `raylib/Color` -> ()"
 {:arglists
  '([center
     innerRadius
     outerRadius
     startAngle
     endAngle
     segments
     color])}
 DrawRingLines
 [:raylib/Vector2
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangle
 "`int` `int` `int` `int` `raylib/Color` -> ()"
 {:arglists '([posX posY width height color])}
 DrawRectangle
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangleV
 "`raylib/Vector2` `raylib/Vector2` `raylib/Color` -> ()"
 {:arglists '([position size color])}
 DrawRectangleV
 [:raylib/Vector2 :raylib/Vector2 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangleRec
 "`raylib/Rectangle` `raylib/Color` -> ()"
 {:arglists '([rec color])}
 DrawRectangleRec
 [:raylib/Rectangle :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectanglePro
 "`raylib/Rectangle` `raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists '([rec origin rotation color])}
 DrawRectanglePro
 [:raylib/Rectangle :raylib/Vector2 :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangleGradientV
 "`int` `int` `int` `int` `raylib/Color` `raylib/Color` -> ()"
 {:arglists '([posX posY width height color1 color2])}
 DrawRectangleGradientV
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangleGradientH
 "`int` `int` `int` `int` `raylib/Color` `raylib/Color` -> ()"
 {:arglists '([posX posY width height color1 color2])}
 DrawRectangleGradientH
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangleGradientEx
 "`raylib/Rectangle` `raylib/Color` `raylib/Color` `raylib/Color` `raylib/Color` -> ()"
 {:arglists '([rec col1 col2 col3 col4])}
 DrawRectangleGradientEx
 [:raylib/Rectangle
  :raylib/Color
  :raylib/Color
  :raylib/Color
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangleLines
 "`int` `int` `int` `int` `raylib/Color` -> ()"
 {:arglists '([posX posY width height color])}
 DrawRectangleLines
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangleLinesEx
 "`raylib/Rectangle` `float` `raylib/Color` -> ()"
 {:arglists '([rec lineThick color])}
 DrawRectangleLinesEx
 [:raylib/Rectangle :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangleRounded
 "`raylib/Rectangle` `float` `int` `raylib/Color` -> ()"
 {:arglists '([rec roundness segments color])}
 DrawRectangleRounded
 [:raylib/Rectangle :coffi.mem/float :coffi.mem/int :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRectangleRoundedLines
 "`raylib/Rectangle` `float` `int` `float` `raylib/Color` -> ()"
 {:arglists '([rec roundness segments lineThick color])}
 DrawRectangleRoundedLines
 [:raylib/Rectangle
  :coffi.mem/float
  :coffi.mem/int
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTriangle
 "`raylib/Vector2` `raylib/Vector2` `raylib/Vector2` `raylib/Color` -> ()"
 {:arglists '([v1 v2 v3 color])}
 DrawTriangle
 [:raylib/Vector2 :raylib/Vector2 :raylib/Vector2 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTriangleLines
 "`raylib/Vector2` `raylib/Vector2` `raylib/Vector2` `raylib/Color` -> ()"
 {:arglists '([v1 v2 v3 color])}
 DrawTriangleLines
 [:raylib/Vector2 :raylib/Vector2 :raylib/Vector2 :raylib/Color]
 :coffi.mem/void)

(clojure.core/let
 [size-of-raylib__Vector2
  (coffi.mem/size-of :raylib/Vector2)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Vector2
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Vector2)
  alloc-raylib__Vector2-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-raylib__Vector2)
    arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  DrawTriangleFan
  "[`raylib/Vector2`] `raylib/Color` -> ()"
  {:arglists '([points color])}
  DrawTriangleFan
  [:coffi.mem/pointer :coffi.mem/int :raylib/Color]
  :coffi.mem/void
  DrawTriangleFan-native
  [points color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [pointCount
     (clojure.core/count points)
     points'
     (clojure.core/let
      [local-segment (alloc-raylib__Vector2-list pointCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq points) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__Vector2
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__Vector2)))))
      local-segment)
     return-value-raw
     (DrawTriangleFan-native points' pointCount color)
     return-value
     return-value-raw]
    nil))))

(clojure.core/let
 [size-of-raylib__Vector2
  (coffi.mem/size-of :raylib/Vector2)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Vector2
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Vector2)
  alloc-raylib__Vector2-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-raylib__Vector2)
    arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  DrawTriangleStrip
  "[`raylib/Vector2`] `raylib/Color` -> ()"
  {:arglists '([points color])}
  DrawTriangleStrip
  [:coffi.mem/pointer :coffi.mem/int :raylib/Color]
  :coffi.mem/void
  DrawTriangleStrip-native
  [points color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [pointCount
     (clojure.core/count points)
     points'
     (clojure.core/let
      [local-segment (alloc-raylib__Vector2-list pointCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq points) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__Vector2
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__Vector2)))))
      local-segment)
     return-value-raw
     (DrawTriangleStrip-native points' pointCount color)
     return-value
     return-value-raw]
    nil))))

(coffi.ffi/defcfn
 DrawPoly
 "`raylib/Vector2` `int` `float` `float` `raylib/Color` -> ()"
 {:arglists '([center sides radius rotation color])}
 DrawPoly
 [:raylib/Vector2
  :coffi.mem/int
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawPolyLines
 "`raylib/Vector2` `int` `float` `float` `raylib/Color` -> ()"
 {:arglists '([center sides radius rotation color])}
 DrawPolyLines
 [:raylib/Vector2
  :coffi.mem/int
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawPolyLinesEx
 "`raylib/Vector2` `int` `float` `float` `float` `raylib/Color` -> ()"
 {:arglists '([center sides radius rotation lineThick color])}
 DrawPolyLinesEx
 [:raylib/Vector2
  :coffi.mem/int
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 CheckCollisionRecs
 "`raylib/Rectangle` `raylib/Rectangle` -> `coffimaker.runtime/bool`"
 {:arglists '([rec1 rec2])}
 CheckCollisionRecs
 [:raylib/Rectangle :raylib/Rectangle]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 CheckCollisionCircles
 "`raylib/Vector2` `float` `raylib/Vector2` `float` -> `coffimaker.runtime/bool`"
 {:arglists '([center1 radius1 center2 radius2])}
 CheckCollisionCircles
 [:raylib/Vector2 :coffi.mem/float :raylib/Vector2 :coffi.mem/float]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 CheckCollisionCircleRec
 "`raylib/Vector2` `float` `raylib/Rectangle` -> `coffimaker.runtime/bool`"
 {:arglists '([center radius rec])}
 CheckCollisionCircleRec
 [:raylib/Vector2 :coffi.mem/float :raylib/Rectangle]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 CheckCollisionPointRec
 "`raylib/Vector2` `raylib/Rectangle` -> `coffimaker.runtime/bool`"
 {:arglists '([point rec])}
 CheckCollisionPointRec
 [:raylib/Vector2 :raylib/Rectangle]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 CheckCollisionPointCircle
 "`raylib/Vector2` `raylib/Vector2` `float` -> `coffimaker.runtime/bool`"
 {:arglists '([point center radius])}
 CheckCollisionPointCircle
 [:raylib/Vector2 :raylib/Vector2 :coffi.mem/float]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 CheckCollisionPointTriangle
 "`raylib/Vector2` `raylib/Vector2` `raylib/Vector2` `raylib/Vector2` -> `coffimaker.runtime/bool`"
 {:arglists '([point p1 p2 p3])}
 CheckCollisionPointTriangle
 [:raylib/Vector2 :raylib/Vector2 :raylib/Vector2 :raylib/Vector2]
 :coffimaker.runtime/bool)

(clojure.core/let
 [size-of-raylib__Vector2
  (coffi.mem/size-of :raylib/Vector2)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Vector2
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Vector2)
  alloc-raylib__Vector2-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-raylib__Vector2)
    arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  CheckCollisionPointPoly
  "`raylib/Vector2` [`raylib/Vector2`] -> `coffimaker.runtime/bool`"
  {:arglists '([point points])}
  CheckCollisionPointPoly
  [:raylib/Vector2 :coffi.mem/pointer :coffi.mem/int]
  :coffimaker.runtime/bool
  CheckCollisionPointPoly-native
  [point points]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [pointCount
     (clojure.core/count points)
     points'
     (clojure.core/let
      [local-segment (alloc-raylib__Vector2-list pointCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq points) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__Vector2
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__Vector2)))))
      local-segment)
     return-value-raw
     (CheckCollisionPointPoly-native point points' pointCount)
     return-value
     return-value-raw]
    return-value))))

(clojure.core/let
 [size-of-raylib__Vector2
  (coffi.mem/size-of :raylib/Vector2)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  deserialize-from-raylib__Vector2
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Vector2)
  alloc-raylib__Vector2
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Vector2 arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  CheckCollisionLines
  "`raylib/Vector2` `raylib/Vector2` `raylib/Vector2` `raylib/Vector2` -> `coffimaker.runtime/bool`"
  {:arglists '([startPos1 endPos1 startPos2 endPos2])}
  CheckCollisionLines
  [:raylib/Vector2
   :raylib/Vector2
   :raylib/Vector2
   :raylib/Vector2
   :coffi.mem/pointer]
  :coffimaker.runtime/bool
  CheckCollisionLines-native
  [startPos1 endPos1 startPos2 endPos2]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [collisionPoint'
     (alloc-raylib__Vector2 arena)
     return-value-raw
     (CheckCollisionLines-native
      startPos1
      endPos1
      startPos2
      endPos2
      collisionPoint')
     collisionPoint
     (deserialize-from-raylib__Vector2
      (.reinterpret
       ^java.lang.foreign.MemorySegment collisionPoint'
       size-of-raylib__Vector2)
      nil)
     return-value
     return-value-raw]
    {:return-value return-value, :collisionPoint collisionPoint}))))

(coffi.ffi/defcfn
 CheckCollisionPointLine
 "`raylib/Vector2` `raylib/Vector2` `raylib/Vector2` `int` -> `coffimaker.runtime/bool`"
 {:arglists '([point p1 p2 threshold])}
 CheckCollisionPointLine
 [:raylib/Vector2 :raylib/Vector2 :raylib/Vector2 :coffi.mem/int]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GetCollisionRec
 "`raylib/Rectangle` `raylib/Rectangle` -> `raylib/Rectangle`"
 {:arglists '([rec1 rec2])}
 GetCollisionRec
 [:raylib/Rectangle :raylib/Rectangle]
 :raylib/Rectangle)

(coffi.ffi/defcfn
 LoadImage
 "`string` -> `raylib/Image`"
 {:arglists '([fileName])}
 LoadImage
 [:coffi.mem/c-string]
 :raylib/Image)

(coffi.ffi/defcfn
 LoadImageRaw
 "`string` `int` `int` `int` `int` -> `raylib/Image`"
 {:arglists '([fileName width height format headerSize])}
 LoadImageRaw
 [:coffi.mem/c-string
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int]
 :raylib/Image)

(clojure.core/let
 [size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))]
 (coffi.ffi/defcfn
  LoadImageAnim
  "`string` -> `raylib/Image`"
  {:arglists '([fileName])}
  LoadImageAnim
  [:coffi.mem/c-string :coffi.mem/pointer]
  :raylib/Image
  LoadImageAnim-native
  [fileName]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [frames'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (LoadImageAnim-native fileName frames')
     frames
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment frames' 4)
      0)
     return-value
     return-value-raw]
    {:return-value return-value, :frames frames}))))

(coffi.ffi/defcfn
 LoadImageFromMemory
 "`string` `string` `int` -> `raylib/Image`"
 {:arglists '([fileType fileData dataSize])}
 LoadImageFromMemory
 [:coffi.mem/c-string :coffi.mem/c-string :coffi.mem/int]
 :raylib/Image)

(coffi.ffi/defcfn
 LoadImageFromTexture
 "`raylib/Texture` -> `raylib/Image`"
 {:arglists '([texture])}
 LoadImageFromTexture
 [:raylib/Texture]
 :raylib/Image)

(coffi.ffi/defcfn
 LoadImageFromScreen
 "() -> `raylib/Image`"
 {:arglists '([])}
 LoadImageFromScreen
 []
 :raylib/Image)

(coffi.ffi/defcfn
 IsImageReady
 "`raylib/Image` -> `coffimaker.runtime/bool`"
 {:arglists '([image])}
 IsImageReady
 [:raylib/Image]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 UnloadImage
 "`raylib/Image` -> ()"
 {:arglists '([image])}
 UnloadImage
 [:raylib/Image]
 :coffi.mem/void)

(coffi.ffi/defcfn
 ExportImage
 "`raylib/Image` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([image fileName])}
 ExportImage
 [:raylib/Image :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(clojure.core/let
 [size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  size-of-coffi_mem__byte
  (coffi.mem/size-of :coffi.mem/byte)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  deserialize-from-coffi_mem__byte
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/byte)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))]
 (coffi.ffi/defcfn
  ExportImageToMemory
  "`raylib/Image` `string` -> [`u8`]"
  {:arglists '([image fileType])}
  ExportImageToMemory
  [:raylib/Image :coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/pointer
  ExportImageToMemory-native
  [image fileType]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [fileSize'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (ExportImageToMemory-native image fileType fileSize')
     fileSize
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment fileSize' 4)
      0)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i fileSize)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (coffi.mem/read-byte
          (.reinterpret
           ^java.lang.foreign.MemorySegment return-value-raw
           (clojure.core/* fileSize size-of-coffi_mem__byte))
          (clojure.core/* 1 i))))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(coffi.ffi/defcfn
 ExportImageAsCode
 "`raylib/Image` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([image fileName])}
 ExportImageAsCode
 [:raylib/Image :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GenImageColor
 "`int` `int` `raylib/Color` -> `raylib/Image`"
 {:arglists '([width height color])}
 GenImageColor
 [:coffi.mem/int :coffi.mem/int :raylib/Color]
 :raylib/Image)

(coffi.ffi/defcfn
 GenImageGradientLinear
 "`int` `int` `int` `raylib/Color` `raylib/Color` -> `raylib/Image`"
 {:arglists '([width height direction start end])}
 GenImageGradientLinear
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color
  :raylib/Color]
 :raylib/Image)

(coffi.ffi/defcfn
 GenImageGradientRadial
 "`int` `int` `float` `raylib/Color` `raylib/Color` -> `raylib/Image`"
 {:arglists '([width height density inner outer])}
 GenImageGradientRadial
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/float
  :raylib/Color
  :raylib/Color]
 :raylib/Image)

(coffi.ffi/defcfn
 GenImageGradientSquare
 "`int` `int` `float` `raylib/Color` `raylib/Color` -> `raylib/Image`"
 {:arglists '([width height density inner outer])}
 GenImageGradientSquare
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/float
  :raylib/Color
  :raylib/Color]
 :raylib/Image)

(coffi.ffi/defcfn
 GenImageChecked
 "`int` `int` `int` `int` `raylib/Color` `raylib/Color` -> `raylib/Image`"
 {:arglists '([width height checksX checksY col1 col2])}
 GenImageChecked
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color
  :raylib/Color]
 :raylib/Image)

(coffi.ffi/defcfn
 GenImageWhiteNoise
 "`int` `int` `float` -> `raylib/Image`"
 {:arglists '([width height factor])}
 GenImageWhiteNoise
 [:coffi.mem/int :coffi.mem/int :coffi.mem/float]
 :raylib/Image)

(coffi.ffi/defcfn
 GenImagePerlinNoise
 "`int` `int` `int` `int` `float` -> `raylib/Image`"
 {:arglists '([width height offsetX offsetY scale])}
 GenImagePerlinNoise
 [:coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/float]
 :raylib/Image)

(coffi.ffi/defcfn
 GenImageCellular
 "`int` `int` `int` -> `raylib/Image`"
 {:arglists '([width height tileSize])}
 GenImageCellular
 [:coffi.mem/int :coffi.mem/int :coffi.mem/int]
 :raylib/Image)

(coffi.ffi/defcfn
 GenImageText
 "`int` `int` `string` -> `raylib/Image`"
 {:arglists '([width height text])}
 GenImageText
 [:coffi.mem/int :coffi.mem/int :coffi.mem/c-string]
 :raylib/Image)

(coffi.ffi/defcfn
 ImageCopy
 "`raylib/Image` -> `raylib/Image`"
 {:arglists '([image])}
 ImageCopy
 [:raylib/Image]
 :raylib/Image)

(coffi.ffi/defcfn
 ImageFromImage
 "`raylib/Image` `raylib/Rectangle` -> `raylib/Image`"
 {:arglists '([image rec])}
 ImageFromImage
 [:raylib/Image :raylib/Rectangle]
 :raylib/Image)

(coffi.ffi/defcfn
 ImageText
 "`string` `int` `raylib/Color` -> `raylib/Image`"
 {:arglists '([text fontSize color])}
 ImageText
 [:coffi.mem/c-string :coffi.mem/int :raylib/Color]
 :raylib/Image)

(coffi.ffi/defcfn
 ImageTextEx
 "`raylib/Font` `string` `float` `float` `raylib/Color` -> `raylib/Image`"
 {:arglists '([font text fontSize spacing tint])}
 ImageTextEx
 [:raylib/Font
  :coffi.mem/c-string
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :raylib/Image)

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageFormat
  "`raylib/Image` `int` -> `raylib/Image`"
  {:arglists '([image newFormat])}
  ImageFormat
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void
  ImageFormat-native
  [image! newFormat]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageFormat-native image' newFormat)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageToPOT
  "`raylib/Image` `raylib/Color` -> `raylib/Image`"
  {:arglists '([image fill])}
  ImageToPOT
  [:coffi.mem/pointer :raylib/Color]
  :coffi.mem/void
  ImageToPOT-native
  [image! fill]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageToPOT-native image' fill)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageCrop
  "`raylib/Image` `raylib/Rectangle` -> `raylib/Image`"
  {:arglists '([image crop])}
  ImageCrop
  [:coffi.mem/pointer :raylib/Rectangle]
  :coffi.mem/void
  ImageCrop-native
  [image! crop]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageCrop-native image' crop)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageAlphaCrop
  "`raylib/Image` `float` -> `raylib/Image`"
  {:arglists '([image threshold])}
  ImageAlphaCrop
  [:coffi.mem/pointer :coffi.mem/float]
  :coffi.mem/void
  ImageAlphaCrop-native
  [image! threshold]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageAlphaCrop-native image' threshold)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageAlphaClear
  "`raylib/Image` `raylib/Color` `float` -> `raylib/Image`"
  {:arglists '([image color threshold])}
  ImageAlphaClear
  [:coffi.mem/pointer :raylib/Color :coffi.mem/float]
  :coffi.mem/void
  ImageAlphaClear-native
  [image! color threshold]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageAlphaClear-native image' color threshold)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageAlphaMask
  "`raylib/Image` `raylib/Image` -> `raylib/Image`"
  {:arglists '([image alphaMask])}
  ImageAlphaMask
  [:coffi.mem/pointer :raylib/Image]
  :coffi.mem/void
  ImageAlphaMask-native
  [image! alphaMask]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageAlphaMask-native image' alphaMask)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageAlphaPremultiply
  "`raylib/Image` -> `raylib/Image`"
  {:arglists '([image])}
  ImageAlphaPremultiply
  [:coffi.mem/pointer]
  :coffi.mem/void
  ImageAlphaPremultiply-native
  [image!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageAlphaPremultiply-native image')
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageBlurGaussian
  "`raylib/Image` `int` -> `raylib/Image`"
  {:arglists '([image blurSize])}
  ImageBlurGaussian
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void
  ImageBlurGaussian-native
  [image! blurSize]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageBlurGaussian-native image' blurSize)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageResize
  "`raylib/Image` `int` `int` -> `raylib/Image`"
  {:arglists '([image newWidth newHeight])}
  ImageResize
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void
  ImageResize-native
  [image! newWidth newHeight]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageResize-native image' newWidth newHeight)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageResizeNN
  "`raylib/Image` `int` `int` -> `raylib/Image`"
  {:arglists '([image newWidth newHeight])}
  ImageResizeNN
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void
  ImageResizeNN-native
  [image! newWidth newHeight]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageResizeNN-native image' newWidth newHeight)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageResizeCanvas
  "`raylib/Image` `int` `int` `int` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([image newWidth newHeight offsetX offsetY fill])}
  ImageResizeCanvas
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib/Color]
  :coffi.mem/void
  ImageResizeCanvas-native
  [image! newWidth newHeight offsetX offsetY fill]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageResizeCanvas-native
      image'
      newWidth
      newHeight
      offsetX
      offsetY
      fill)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageMipmaps
  "`raylib/Image` -> `raylib/Image`"
  {:arglists '([image])}
  ImageMipmaps
  [:coffi.mem/pointer]
  :coffi.mem/void
  ImageMipmaps-native
  [image!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageMipmaps-native image')
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDither
  "`raylib/Image` `int` `int` `int` `int` -> `raylib/Image`"
  {:arglists '([image rBpp gBpp bBpp aBpp])}
  ImageDither
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int]
  :coffi.mem/void
  ImageDither-native
  [image! rBpp gBpp bBpp aBpp]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDither-native image' rBpp gBpp bBpp aBpp)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageFlipVertical
  "`raylib/Image` -> `raylib/Image`"
  {:arglists '([image])}
  ImageFlipVertical
  [:coffi.mem/pointer]
  :coffi.mem/void
  ImageFlipVertical-native
  [image!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageFlipVertical-native image')
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageFlipHorizontal
  "`raylib/Image` -> `raylib/Image`"
  {:arglists '([image])}
  ImageFlipHorizontal
  [:coffi.mem/pointer]
  :coffi.mem/void
  ImageFlipHorizontal-native
  [image!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageFlipHorizontal-native image')
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageRotate
  "`raylib/Image` `int` -> `raylib/Image`"
  {:arglists '([image degrees])}
  ImageRotate
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void
  ImageRotate-native
  [image! degrees]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageRotate-native image' degrees)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageRotateCW
  "`raylib/Image` -> `raylib/Image`"
  {:arglists '([image])}
  ImageRotateCW
  [:coffi.mem/pointer]
  :coffi.mem/void
  ImageRotateCW-native
  [image!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageRotateCW-native image')
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageRotateCCW
  "`raylib/Image` -> `raylib/Image`"
  {:arglists '([image])}
  ImageRotateCCW
  [:coffi.mem/pointer]
  :coffi.mem/void
  ImageRotateCCW-native
  [image!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageRotateCCW-native image')
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageColorTint
  "`raylib/Image` `raylib/Color` -> `raylib/Image`"
  {:arglists '([image color])}
  ImageColorTint
  [:coffi.mem/pointer :raylib/Color]
  :coffi.mem/void
  ImageColorTint-native
  [image! color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageColorTint-native image' color)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageColorInvert
  "`raylib/Image` -> `raylib/Image`"
  {:arglists '([image])}
  ImageColorInvert
  [:coffi.mem/pointer]
  :coffi.mem/void
  ImageColorInvert-native
  [image!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageColorInvert-native image')
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageColorGrayscale
  "`raylib/Image` -> `raylib/Image`"
  {:arglists '([image])}
  ImageColorGrayscale
  [:coffi.mem/pointer]
  :coffi.mem/void
  ImageColorGrayscale-native
  [image!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageColorGrayscale-native image')
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageColorContrast
  "`raylib/Image` `float` -> `raylib/Image`"
  {:arglists '([image contrast])}
  ImageColorContrast
  [:coffi.mem/pointer :coffi.mem/float]
  :coffi.mem/void
  ImageColorContrast-native
  [image! contrast]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageColorContrast-native image' contrast)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageColorBrightness
  "`raylib/Image` `int` -> `raylib/Image`"
  {:arglists '([image brightness])}
  ImageColorBrightness
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void
  ImageColorBrightness-native
  [image! brightness]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageColorBrightness-native image' brightness)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageColorReplace
  "`raylib/Image` `raylib/Color` `raylib/Color` -> `raylib/Image`"
  {:arglists '([image color replace])}
  ImageColorReplace
  [:coffi.mem/pointer :raylib/Color :raylib/Color]
  :coffi.mem/void
  ImageColorReplace-native
  [image! color replace]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [image'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image image! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageColorReplace-native image' color replace)
     image
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment image'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    image))))

(coffi.ffi/defcfn
 LoadImageColors
 "`raylib/Image` -> *`raylib/Color`"
 {:arglists '([image])}
 LoadImageColors
 [:raylib/Image]
 :coffi.mem/pointer)

(clojure.core/let
 [size-of-raylib__Color
  (coffi.mem/size-of :raylib/Color)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  deserialize-from-raylib__Color
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Color)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))]
 (coffi.ffi/defcfn
  LoadImagePalette
  "`raylib/Image` `int` -> [`raylib/Color`]"
  {:arglists '([image maxPaletteSize])}
  LoadImagePalette
  [:raylib/Image :coffi.mem/int :coffi.mem/pointer]
  :coffi.mem/pointer
  LoadImagePalette-native
  [image maxPaletteSize]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [colorCount'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (LoadImagePalette-native image maxPaletteSize colorCount')
     colorCount
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment colorCount' 4)
      0)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i colorCount)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (deserialize-from-raylib__Color
          (coffimaker.runtime/unsafe-offset
           (.reinterpret
            ^java.lang.foreign.MemorySegment return-value-raw
            (clojure.core/* colorCount size-of-raylib__Color))
           (clojure.core/* size-of-raylib__Color i))
          nil)))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(coffi.ffi/defcfn
 UnloadImageColors
 "*`raylib/Color` -> ()"
 {:arglists '([colors])}
 UnloadImageColors
 [:coffi.mem/pointer]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UnloadImagePalette
 "*`raylib/Color` -> ()"
 {:arglists '([colors])}
 UnloadImagePalette
 [:coffi.mem/pointer]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetImageAlphaBorder
 "`raylib/Image` `float` -> `raylib/Rectangle`"
 {:arglists '([image threshold])}
 GetImageAlphaBorder
 [:raylib/Image :coffi.mem/float]
 :raylib/Rectangle)

(coffi.ffi/defcfn
 GetImageColor
 "`raylib/Image` `int` `int` -> `raylib/Color`"
 {:arglists '([image x y])}
 GetImageColor
 [:raylib/Image :coffi.mem/int :coffi.mem/int]
 :raylib/Color)

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageClearBackground
  "`raylib/Image` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst color])}
  ImageClearBackground
  [:coffi.mem/pointer :raylib/Color]
  :coffi.mem/void
  ImageClearBackground-native
  [dst! color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageClearBackground-native dst' color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawPixel
  "`raylib/Image` `int` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst posX posY color])}
  ImageDrawPixel
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int :raylib/Color]
  :coffi.mem/void
  ImageDrawPixel-native
  [dst! posX posY color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawPixel-native dst' posX posY color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawPixelV
  "`raylib/Image` `raylib/Vector2` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst position color])}
  ImageDrawPixelV
  [:coffi.mem/pointer :raylib/Vector2 :raylib/Color]
  :coffi.mem/void
  ImageDrawPixelV-native
  [dst! position color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawPixelV-native dst' position color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawLine
  "`raylib/Image` `int` `int` `int` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst startPosX startPosY endPosX endPosY color])}
  ImageDrawLine
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib/Color]
  :coffi.mem/void
  ImageDrawLine-native
  [dst! startPosX startPosY endPosX endPosY color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawLine-native
      dst'
      startPosX
      startPosY
      endPosX
      endPosY
      color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawLineV
  "`raylib/Image` `raylib/Vector2` `raylib/Vector2` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst start end color])}
  ImageDrawLineV
  [:coffi.mem/pointer :raylib/Vector2 :raylib/Vector2 :raylib/Color]
  :coffi.mem/void
  ImageDrawLineV-native
  [dst! start end color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawLineV-native dst' start end color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawCircle
  "`raylib/Image` `int` `int` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst centerX centerY radius color])}
  ImageDrawCircle
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib/Color]
  :coffi.mem/void
  ImageDrawCircle-native
  [dst! centerX centerY radius color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawCircle-native dst' centerX centerY radius color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawCircleV
  "`raylib/Image` `raylib/Vector2` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst center radius color])}
  ImageDrawCircleV
  [:coffi.mem/pointer :raylib/Vector2 :coffi.mem/int :raylib/Color]
  :coffi.mem/void
  ImageDrawCircleV-native
  [dst! center radius color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawCircleV-native dst' center radius color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawCircleLines
  "`raylib/Image` `int` `int` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst centerX centerY radius color])}
  ImageDrawCircleLines
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib/Color]
  :coffi.mem/void
  ImageDrawCircleLines-native
  [dst! centerX centerY radius color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawCircleLines-native dst' centerX centerY radius color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawCircleLinesV
  "`raylib/Image` `raylib/Vector2` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst center radius color])}
  ImageDrawCircleLinesV
  [:coffi.mem/pointer :raylib/Vector2 :coffi.mem/int :raylib/Color]
  :coffi.mem/void
  ImageDrawCircleLinesV-native
  [dst! center radius color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawCircleLinesV-native dst' center radius color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawRectangle
  "`raylib/Image` `int` `int` `int` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst posX posY width height color])}
  ImageDrawRectangle
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib/Color]
  :coffi.mem/void
  ImageDrawRectangle-native
  [dst! posX posY width height color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawRectangle-native dst' posX posY width height color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawRectangleV
  "`raylib/Image` `raylib/Vector2` `raylib/Vector2` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst position size color])}
  ImageDrawRectangleV
  [:coffi.mem/pointer :raylib/Vector2 :raylib/Vector2 :raylib/Color]
  :coffi.mem/void
  ImageDrawRectangleV-native
  [dst! position size color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawRectangleV-native dst' position size color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawRectangleRec
  "`raylib/Image` `raylib/Rectangle` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst rec color])}
  ImageDrawRectangleRec
  [:coffi.mem/pointer :raylib/Rectangle :raylib/Color]
  :coffi.mem/void
  ImageDrawRectangleRec-native
  [dst! rec color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawRectangleRec-native dst' rec color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawRectangleLines
  "`raylib/Image` `raylib/Rectangle` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst rec thick color])}
  ImageDrawRectangleLines
  [:coffi.mem/pointer :raylib/Rectangle :coffi.mem/int :raylib/Color]
  :coffi.mem/void
  ImageDrawRectangleLines-native
  [dst! rec thick color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawRectangleLines-native dst' rec thick color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDraw
  "`raylib/Image` `raylib/Image` `raylib/Rectangle` `raylib/Rectangle` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst src srcRec dstRec tint])}
  ImageDraw
  [:coffi.mem/pointer
   :raylib/Image
   :raylib/Rectangle
   :raylib/Rectangle
   :raylib/Color]
  :coffi.mem/void
  ImageDraw-native
  [dst! src srcRec dstRec tint]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDraw-native dst' src srcRec dstRec tint)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawText
  "`raylib/Image` `string` `int` `int` `int` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst text posX posY fontSize color])}
  ImageDrawText
  [:coffi.mem/pointer
   :coffi.mem/c-string
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib/Color]
  :coffi.mem/void
  ImageDrawText-native
  [dst! text posX posY fontSize color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawText-native dst' text posX posY fontSize color)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(clojure.core/let
 [size-of-raylib__Image
  (coffi.mem/size-of :raylib/Image)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Image
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Image)
  deserialize-from-raylib__Image
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Image)
  alloc-raylib__Image
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Image arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  ImageDrawTextEx
  "`raylib/Image` `raylib/Font` `string` `raylib/Vector2` `float` `float` `raylib/Color` -> `raylib/Image`"
  {:arglists '([dst font text position fontSize spacing tint])}
  ImageDrawTextEx
  [:coffi.mem/pointer
   :raylib/Font
   :coffi.mem/c-string
   :raylib/Vector2
   :coffi.mem/float
   :coffi.mem/float
   :raylib/Color]
  :coffi.mem/void
  ImageDrawTextEx-native
  [dst! font text position fontSize spacing tint]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dst'
     (clojure.core/let
      [local-segment (alloc-raylib__Image arena)]
      (serialize-into-raylib__Image dst! nil local-segment nil)
      local-segment)
     return-value-raw
     (ImageDrawTextEx-native
      dst'
      font
      text
      position
      fontSize
      spacing
      tint)
     dst
     (deserialize-from-raylib__Image
      (.reinterpret
       ^java.lang.foreign.MemorySegment dst'
       size-of-raylib__Image)
      nil)
     return-value
     return-value-raw]
    dst))))

(coffi.ffi/defcfn
 LoadTexture
 "`string` -> `raylib/Texture`"
 {:arglists '([fileName])}
 LoadTexture
 [:coffi.mem/c-string]
 :raylib/Texture)

(coffi.ffi/defcfn
 LoadTextureFromImage
 "`raylib/Image` -> `raylib/Texture`"
 {:arglists '([image])}
 LoadTextureFromImage
 [:raylib/Image]
 :raylib/Texture)

(coffi.ffi/defcfn
 LoadTextureCubemap
 "`raylib/Image` `int` -> `raylib/Texture`"
 {:arglists '([image layout])}
 LoadTextureCubemap
 [:raylib/Image :coffi.mem/int]
 :raylib/Texture)

(coffi.ffi/defcfn
 LoadRenderTexture
 "`int` `int` -> `raylib/RenderTexture`"
 {:arglists '([width height])}
 LoadRenderTexture
 [:coffi.mem/int :coffi.mem/int]
 :raylib/RenderTexture)

(coffi.ffi/defcfn
 IsTextureReady
 "`raylib/Texture` -> `coffimaker.runtime/bool`"
 {:arglists '([texture])}
 IsTextureReady
 [:raylib/Texture]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 UnloadTexture
 "`raylib/Texture` -> ()"
 {:arglists '([texture])}
 UnloadTexture
 [:raylib/Texture]
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsRenderTextureReady
 "`raylib/RenderTexture` -> `coffimaker.runtime/bool`"
 {:arglists '([target])}
 IsRenderTextureReady
 [:raylib/RenderTexture]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 UnloadRenderTexture
 "`raylib/RenderTexture` -> ()"
 {:arglists '([target])}
 UnloadRenderTexture
 [:raylib/RenderTexture]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UpdateTexture
 "`raylib/Texture` `pointer` -> ()"
 {:arglists '([texture pixels])}
 UpdateTexture
 [:raylib/Texture :coffi.mem/pointer]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UpdateTextureRec
 "`raylib/Texture` `raylib/Rectangle` `pointer` -> ()"
 {:arglists '([texture rec pixels])}
 UpdateTextureRec
 [:raylib/Texture :raylib/Rectangle :coffi.mem/pointer]
 :coffi.mem/void)

(clojure.core/let
 [size-of-raylib__Texture
  (coffi.mem/size-of :raylib/Texture)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Texture
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Texture)
  deserialize-from-raylib__Texture
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Texture)
  alloc-raylib__Texture
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Texture arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  GenTextureMipmaps
  "`raylib/Texture` -> `raylib/Texture`"
  {:arglists '([texture])}
  GenTextureMipmaps
  [:coffi.mem/pointer]
  :coffi.mem/void
  GenTextureMipmaps-native
  [texture!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [texture'
     (clojure.core/let
      [local-segment (alloc-raylib__Texture arena)]
      (serialize-into-raylib__Texture texture! nil local-segment nil)
      local-segment)
     return-value-raw
     (GenTextureMipmaps-native texture')
     texture
     (deserialize-from-raylib__Texture
      (.reinterpret
       ^java.lang.foreign.MemorySegment texture'
       size-of-raylib__Texture)
      nil)
     return-value
     return-value-raw]
    texture))))

(coffi.ffi/defcfn
 SetTextureFilter
 "`raylib/Texture` `int` -> ()"
 {:arglists '([texture filter])}
 SetTextureFilter
 [:raylib/Texture :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetTextureWrap
 "`raylib/Texture` `int` -> ()"
 {:arglists '([texture wrap])}
 SetTextureWrap
 [:raylib/Texture :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTexture
 "`raylib/Texture` `int` `int` `raylib/Color` -> ()"
 {:arglists '([texture posX posY tint])}
 DrawTexture
 [:raylib/Texture :coffi.mem/int :coffi.mem/int :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTextureV
 "`raylib/Texture` `raylib/Vector2` `raylib/Color` -> ()"
 {:arglists '([texture position tint])}
 DrawTextureV
 [:raylib/Texture :raylib/Vector2 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTextureEx
 "`raylib/Texture` `raylib/Vector2` `float` `float` `raylib/Color` -> ()"
 {:arglists '([texture position rotation scale tint])}
 DrawTextureEx
 [:raylib/Texture
  :raylib/Vector2
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTextureRec
 "`raylib/Texture` `raylib/Rectangle` `raylib/Vector2` `raylib/Color` -> ()"
 {:arglists '([texture source position tint])}
 DrawTextureRec
 [:raylib/Texture :raylib/Rectangle :raylib/Vector2 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTexturePro
 "`raylib/Texture` `raylib/Rectangle` `raylib/Rectangle` `raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists '([texture source dest origin rotation tint])}
 DrawTexturePro
 [:raylib/Texture
  :raylib/Rectangle
  :raylib/Rectangle
  :raylib/Vector2
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTextureNPatch
 "`raylib/Texture` `raylib/NPatchInfo` `raylib/Rectangle` `raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists '([texture nPatchInfo dest origin rotation tint])}
 DrawTextureNPatch
 [:raylib/Texture
  :raylib/NPatchInfo
  :raylib/Rectangle
  :raylib/Vector2
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 Fade
 "`raylib/Color` `float` -> `raylib/Color`"
 {:arglists '([color alpha])}
 Fade
 [:raylib/Color :coffi.mem/float]
 :raylib/Color)

(coffi.ffi/defcfn
 ColorToInt
 "`raylib/Color` -> `int`"
 {:arglists '([color])}
 ColorToInt
 [:raylib/Color]
 :coffi.mem/int)

(coffi.ffi/defcfn
 ColorNormalize
 "`raylib/Color` -> `raylib/Vector4`"
 {:arglists '([color])}
 ColorNormalize
 [:raylib/Color]
 :raylib/Vector4)

(coffi.ffi/defcfn
 ColorFromNormalized
 "`raylib/Vector4` -> `raylib/Color`"
 {:arglists '([normalized])}
 ColorFromNormalized
 [:raylib/Vector4]
 :raylib/Color)

(coffi.ffi/defcfn
 ColorToHSV
 "`raylib/Color` -> `raylib/Vector3`"
 {:arglists '([color])}
 ColorToHSV
 [:raylib/Color]
 :raylib/Vector3)

(coffi.ffi/defcfn
 ColorFromHSV
 "`float` `float` `float` -> `raylib/Color`"
 {:arglists '([hue saturation value])}
 ColorFromHSV
 [:coffi.mem/float :coffi.mem/float :coffi.mem/float]
 :raylib/Color)

(coffi.ffi/defcfn
 ColorTint
 "`raylib/Color` `raylib/Color` -> `raylib/Color`"
 {:arglists '([color tint])}
 ColorTint
 [:raylib/Color :raylib/Color]
 :raylib/Color)

(coffi.ffi/defcfn
 ColorBrightness
 "`raylib/Color` `float` -> `raylib/Color`"
 {:arglists '([color factor])}
 ColorBrightness
 [:raylib/Color :coffi.mem/float]
 :raylib/Color)

(coffi.ffi/defcfn
 ColorContrast
 "`raylib/Color` `float` -> `raylib/Color`"
 {:arglists '([color contrast])}
 ColorContrast
 [:raylib/Color :coffi.mem/float]
 :raylib/Color)

(coffi.ffi/defcfn
 ColorAlpha
 "`raylib/Color` `float` -> `raylib/Color`"
 {:arglists '([color alpha])}
 ColorAlpha
 [:raylib/Color :coffi.mem/float]
 :raylib/Color)

(coffi.ffi/defcfn
 ColorAlphaBlend
 "`raylib/Color` `raylib/Color` `raylib/Color` -> `raylib/Color`"
 {:arglists '([dst src tint])}
 ColorAlphaBlend
 [:raylib/Color :raylib/Color :raylib/Color]
 :raylib/Color)

(coffi.ffi/defcfn
 GetColor
 "`int` -> `raylib/Color`"
 {:arglists '([hexValue])}
 GetColor
 [:coffi.mem/int]
 :raylib/Color)

(coffi.ffi/defcfn
 GetPixelColor
 "`pointer` `int` -> `raylib/Color`"
 {:arglists '([srcPtr format])}
 GetPixelColor
 [:coffi.mem/pointer :coffi.mem/int]
 :raylib/Color)

(coffi.ffi/defcfn
 SetPixelColor
 "`pointer` `raylib/Color` `int` -> ()"
 {:arglists '([dstPtr color format])}
 SetPixelColor
 [:coffi.mem/pointer :raylib/Color :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetPixelDataSize
 "`int` `int` `int` -> `int`"
 {:arglists '([width height format])}
 GetPixelDataSize
 [:coffi.mem/int :coffi.mem/int :coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetFontDefault
 "() -> `raylib/Font`"
 {:arglists '([])}
 GetFontDefault
 []
 :raylib/Font)

(coffi.ffi/defcfn
 LoadFont
 "`string` -> `raylib/Font`"
 {:arglists '([fileName])}
 LoadFont
 [:coffi.mem/c-string]
 :raylib/Font)

(clojure.core/let
 [size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  serialize-into-coffi_mem__int
  (clojure.core/get-method coffi.mem/serialize-into :coffi.mem/int)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-coffi_mem__int)
    arena))]
 (coffi.ffi/defcfn
  LoadFontEx
  "`string` `int` [`i32`] -> `raylib/Font`"
  {:arglists '([fileName fontSize fontChars])}
  LoadFontEx
  [:coffi.mem/c-string
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int]
  :raylib/Font
  LoadFontEx-native
  [fileName fontSize fontChars]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [glyphCount
     (clojure.core/count fontChars)
     fontChars'
     (clojure.core/let
      [local-segment (alloc-coffi_mem__int-list glyphCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq fontChars) offset 0]
       (if
        xs
        (do
         (coffi.mem/write-int
          local-segment
          offset
          (clojure.core/first xs))
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-coffi_mem__int)))))
      local-segment)
     return-value-raw
     (LoadFontEx-native fileName fontSize fontChars' glyphCount)
     return-value
     return-value-raw]
    return-value))))

(coffi.ffi/defcfn
 LoadFontFromImage
 "`raylib/Image` `raylib/Color` `int` -> `raylib/Font`"
 {:arglists '([image key firstChar])}
 LoadFontFromImage
 [:raylib/Image :raylib/Color :coffi.mem/int]
 :raylib/Font)

(clojure.core/let
 [size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  size-of-coffi_mem__byte
  (coffi.mem/size-of :coffi.mem/byte)
  serialize-into-coffi_mem__int
  (clojure.core/get-method coffi.mem/serialize-into :coffi.mem/int)
  serialize-into-coffi_mem__byte
  (clojure.core/get-method coffi.mem/serialize-into :coffi.mem/byte)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc (clojure.core/* len size-of-coffi_mem__int) arena))
  alloc-coffi_mem__byte-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-coffi_mem__byte)
    arena))]
 (coffi.ffi/defcfn
  LoadFontFromMemory
  "`string` [`u8`] `int` [`i32`] -> `raylib/Font`"
  {:arglists '([fileType fileData fontSize fontChars])}
  LoadFontFromMemory
  [:coffi.mem/c-string
   :coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int]
  :raylib/Font
  LoadFontFromMemory-native
  [fileType fileData fontSize fontChars]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dataSize
     (clojure.core/count fileData)
     glyphCount
     (clojure.core/count fontChars)
     fileData'
     (clojure.core/let
      [local-segment (alloc-coffi_mem__byte-list dataSize arena)]
      (clojure.core/loop
       [xs (clojure.core/seq fileData) offset 0]
       (if
        xs
        (do
         (coffi.mem/write-byte
          local-segment
          offset
          (clojure.core/first xs))
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-coffi_mem__byte)))))
      local-segment)
     fontChars'
     (clojure.core/let
      [local-segment (alloc-coffi_mem__int-list glyphCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq fontChars) offset 0]
       (if
        xs
        (do
         (coffi.mem/write-int
          local-segment
          offset
          (clojure.core/first xs))
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-coffi_mem__int)))))
      local-segment)
     return-value-raw
     (LoadFontFromMemory-native
      fileType
      fileData'
      dataSize
      fontSize
      fontChars'
      glyphCount)
     return-value
     return-value-raw]
    return-value))))

(coffi.ffi/defcfn
 IsFontReady
 "`raylib/Font` -> `coffimaker.runtime/bool`"
 {:arglists '([font])}
 IsFontReady
 [:raylib/Font]
 :coffimaker.runtime/bool)

(clojure.core/let
 [size-of-raylib__GlyphInfo
  (coffi.mem/size-of :raylib/GlyphInfo)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  size-of-coffi_mem__byte
  (coffi.mem/size-of :coffi.mem/byte)
  serialize-into-coffi_mem__int
  (clojure.core/get-method coffi.mem/serialize-into :coffi.mem/int)
  serialize-into-coffi_mem__byte
  (clojure.core/get-method coffi.mem/serialize-into :coffi.mem/byte)
  deserialize-from-raylib__GlyphInfo
  (clojure.core/get-method
   coffi.mem/deserialize-from
   :raylib/GlyphInfo)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc (clojure.core/* len size-of-coffi_mem__int) arena))
  alloc-coffi_mem__byte-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-coffi_mem__byte)
    arena))]
 (coffi.ffi/defcfn
  LoadFontData
  "[`u8`] `int` [`i32`] `int` -> [`raylib/GlyphInfo`]"
  {:arglists '([fileData fontSize fontChars type])}
  LoadFontData
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int]
  :coffi.mem/pointer
  LoadFontData-native
  [fileData fontSize fontChars type]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [dataSize
     (clojure.core/count fileData)
     glyphCount
     (clojure.core/count fontChars)
     fileData'
     (clojure.core/let
      [local-segment (alloc-coffi_mem__byte-list dataSize arena)]
      (clojure.core/loop
       [xs (clojure.core/seq fileData) offset 0]
       (if
        xs
        (do
         (coffi.mem/write-byte
          local-segment
          offset
          (clojure.core/first xs))
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-coffi_mem__byte)))))
      local-segment)
     fontChars'
     (clojure.core/let
      [local-segment (alloc-coffi_mem__int-list glyphCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq fontChars) offset 0]
       (if
        xs
        (do
         (coffi.mem/write-int
          local-segment
          offset
          (clojure.core/first xs))
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-coffi_mem__int)))))
      local-segment)
     return-value-raw
     (LoadFontData-native
      fileData'
      dataSize
      fontSize
      fontChars'
      glyphCount
      type)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i glyphCount)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (deserialize-from-raylib__GlyphInfo
          (coffimaker.runtime/unsafe-offset
           (.reinterpret
            ^java.lang.foreign.MemorySegment return-value-raw
            (clojure.core/* glyphCount size-of-raylib__GlyphInfo))
           (clojure.core/* size-of-raylib__GlyphInfo i))
          nil)))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(clojure.core/let
 [size-of-raylib__Rectangle
  (coffi.mem/size-of :raylib/Rectangle)
  size-of-raylib__GlyphInfo
  (coffi.mem/size-of :raylib/GlyphInfo)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__GlyphInfo
  (clojure.core/get-method coffi.mem/serialize-into :raylib/GlyphInfo)
  deserialize-from-raylib__Rectangle
  (clojure.core/get-method
   coffi.mem/deserialize-from
   :raylib/Rectangle)
  alloc-raylib__GlyphInfo-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-raylib__GlyphInfo)
    arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  GenImageFontAtlas
  "[`raylib/GlyphInfo`] `int` `int` `int` -> `raylib/Image`"
  {:arglists '([chars fontSize padding packMethod])}
  GenImageFontAtlas
  [:coffi.mem/pointer
   :coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int]
  :raylib/Image
  GenImageFontAtlas-native
  [chars fontSize padding packMethod]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [glyphCount
     (clojure.core/count chars)
     chars'
     (clojure.core/let
      [local-segment (alloc-raylib__GlyphInfo-list glyphCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq chars) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__GlyphInfo
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__GlyphInfo)))))
      local-segment)
     recs'
     (alloc-coffi_mem__pointer arena)
     return-value-raw
     (GenImageFontAtlas-native
      chars'
      recs'
      glyphCount
      fontSize
      padding
      packMethod)
     return-value
     return-value-raw
     recs
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i glyphCount)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (deserialize-from-raylib__Rectangle
          (coffimaker.runtime/unsafe-offset
           (.reinterpret
            ^java.lang.foreign.MemorySegment recs'
            (clojure.core/* glyphCount size-of-raylib__Rectangle))
           (clojure.core/* size-of-raylib__Rectangle i))
          nil)))
       (clojure.core/persistent! v)))]
    {:return-value return-value, :recs recs}))))

(coffi.ffi/defcfn
 UnloadFontData
 "*`raylib/GlyphInfo` `int` -> ()"
 {:arglists '([chars glyphCount])}
 UnloadFontData
 [:coffi.mem/pointer :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UnloadFont
 "`raylib/Font` -> ()"
 {:arglists '([font])}
 UnloadFont
 [:raylib/Font]
 :coffi.mem/void)

(coffi.ffi/defcfn
 ExportFontAsCode
 "`raylib/Font` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([font fileName])}
 ExportFontAsCode
 [:raylib/Font :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 DrawFPS
 "`int` `int` -> ()"
 {:arglists '([posX posY])}
 DrawFPS
 [:coffi.mem/int :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawText
 "`string` `int` `int` `int` `raylib/Color` -> ()"
 {:arglists '([text posX posY fontSize color])}
 DrawText
 [:coffi.mem/c-string
  :coffi.mem/int
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTextEx
 "`raylib/Font` `string` `raylib/Vector2` `float` `float` `raylib/Color` -> ()"
 {:arglists '([font text position fontSize spacing tint])}
 DrawTextEx
 [:raylib/Font
  :coffi.mem/c-string
  :raylib/Vector2
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTextPro
 "`raylib/Font` `string` `raylib/Vector2` `raylib/Vector2` `float` `float` `float` `raylib/Color` -> ()"
 {:arglists
  '([font text position origin rotation fontSize spacing tint])}
 DrawTextPro
 [:raylib/Font
  :coffi.mem/c-string
  :raylib/Vector2
  :raylib/Vector2
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTextCodepoint
 "`raylib/Font` `int` `raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists '([font codepoint position fontSize tint])}
 DrawTextCodepoint
 [:raylib/Font
  :coffi.mem/int
  :raylib/Vector2
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTextCodepoints
 "`raylib/Font` *`int` `int` `raylib/Vector2` `float` `float` `raylib/Color` -> ()"
 {:arglists '([font codepoints count position fontSize spacing tint])}
 DrawTextCodepoints
 [:raylib/Font
  :coffi.mem/pointer
  :coffi.mem/int
  :raylib/Vector2
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetTextLineSpacing
 "`int` -> ()"
 {:arglists '([spacing])}
 SetTextLineSpacing
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 MeasureText
 "`string` `int` -> `int`"
 {:arglists '([text fontSize])}
 MeasureText
 [:coffi.mem/c-string :coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 MeasureTextEx
 "`raylib/Font` `string` `float` `float` -> `raylib/Vector2`"
 {:arglists '([font text fontSize spacing])}
 MeasureTextEx
 [:raylib/Font :coffi.mem/c-string :coffi.mem/float :coffi.mem/float]
 :raylib/Vector2)

(coffi.ffi/defcfn
 GetGlyphIndex
 "`raylib/Font` `int` -> `int`"
 {:arglists '([font codepoint])}
 GetGlyphIndex
 [:raylib/Font :coffi.mem/int]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetGlyphInfo
 "`raylib/Font` `int` -> `raylib/GlyphInfo`"
 {:arglists '([font codepoint])}
 GetGlyphInfo
 [:raylib/Font :coffi.mem/int]
 :raylib/GlyphInfo)

(coffi.ffi/defcfn
 GetGlyphAtlasRec
 "`raylib/Font` `int` -> `raylib/Rectangle`"
 {:arglists '([font codepoint])}
 GetGlyphAtlasRec
 [:raylib/Font :coffi.mem/int]
 :raylib/Rectangle)

(coffi.ffi/defcfn
 LoadUTF8
 "*`int` `int` -> `string`"
 {:arglists '([codepoints length])}
 LoadUTF8
 [:coffi.mem/pointer :coffi.mem/int]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 UnloadUTF8
 "`string` -> ()"
 {:arglists '([text])}
 UnloadUTF8
 [:coffi.mem/c-string]
 :coffi.mem/void)

(coffi.ffi/defcfn
 LoadCodepoints
 "`string` *`int` -> *`int`"
 {:arglists '([text count])}
 LoadCodepoints
 [:coffi.mem/c-string :coffi.mem/pointer]
 :coffi.mem/pointer)

(coffi.ffi/defcfn
 UnloadCodepoints
 "*`int` -> ()"
 {:arglists '([codepoints])}
 UnloadCodepoints
 [:coffi.mem/pointer]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetCodepointCount
 "`string` -> `int`"
 {:arglists '([text])}
 GetCodepointCount
 [:coffi.mem/c-string]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetCodepoint
 "`string` *`int` -> `int`"
 {:arglists '([text codepointSize])}
 GetCodepoint
 [:coffi.mem/c-string :coffi.mem/pointer]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetCodepointNext
 "`string` *`int` -> `int`"
 {:arglists '([text codepointSize])}
 GetCodepointNext
 [:coffi.mem/c-string :coffi.mem/pointer]
 :coffi.mem/int)

(coffi.ffi/defcfn
 GetCodepointPrevious
 "`string` *`int` -> `int`"
 {:arglists '([text codepointSize])}
 GetCodepointPrevious
 [:coffi.mem/c-string :coffi.mem/pointer]
 :coffi.mem/int)

(coffi.ffi/defcfn
 CodepointToUTF8
 "`int` *`int` -> `string`"
 {:arglists '([codepoint utf8Size])}
 CodepointToUTF8
 [:coffi.mem/int :coffi.mem/pointer]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 TextCopy
 "`string` `string` -> `int`"
 {:arglists '([dst src])}
 TextCopy
 [:coffi.mem/c-string :coffi.mem/c-string]
 :coffi.mem/int)

(coffi.ffi/defcfn
 TextIsEqual
 "`string` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([text1 text2])}
 TextIsEqual
 [:coffi.mem/c-string :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 TextLength
 "`string` -> `int`"
 {:arglists '([text])}
 TextLength
 [:coffi.mem/c-string]
 :coffi.mem/int)

(coffi.ffi/defcfn
 TextFormat
 "`string` -> `string`"
 {:arglists '([text])}
 TextFormat
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 TextSubtext
 "`string` `int` `int` -> `string`"
 {:arglists '([text position length])}
 TextSubtext
 [:coffi.mem/c-string :coffi.mem/int :coffi.mem/int]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 TextReplace
 "`string` `string` `string` -> `string`"
 {:arglists '([text replace by])}
 TextReplace
 [:coffi.mem/c-string :coffi.mem/c-string :coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 TextInsert
 "`string` `string` `int` -> `string`"
 {:arglists '([text insert position])}
 TextInsert
 [:coffi.mem/c-string :coffi.mem/c-string :coffi.mem/int]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 TextJoin
 "*`string` `int` `string` -> `string`"
 {:arglists '([textList count delimiter])}
 TextJoin
 [:coffi.mem/pointer :coffi.mem/int :coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 TextSplit
 "`string` `byte` *`int` -> *`string`"
 {:arglists '([text delimiter count])}
 TextSplit
 [:coffi.mem/c-string :coffi.mem/byte :coffi.mem/pointer]
 :coffi.mem/pointer)

(coffi.ffi/defcfn
 TextAppend
 "`string` `string` *`int` -> ()"
 {:arglists '([text append position])}
 TextAppend
 [:coffi.mem/c-string :coffi.mem/c-string :coffi.mem/pointer]
 :coffi.mem/void)

(coffi.ffi/defcfn
 TextFindIndex
 "`string` `string` -> `int`"
 {:arglists '([text find])}
 TextFindIndex
 [:coffi.mem/c-string :coffi.mem/c-string]
 :coffi.mem/int)

(coffi.ffi/defcfn
 TextToUpper
 "`string` -> `string`"
 {:arglists '([text])}
 TextToUpper
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 TextToLower
 "`string` -> `string`"
 {:arglists '([text])}
 TextToLower
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 TextToPascal
 "`string` -> `string`"
 {:arglists '([text])}
 TextToPascal
 [:coffi.mem/c-string]
 :coffi.mem/c-string)

(coffi.ffi/defcfn
 TextToInteger
 "`string` -> `int`"
 {:arglists '([text])}
 TextToInteger
 [:coffi.mem/c-string]
 :coffi.mem/int)

(coffi.ffi/defcfn
 DrawLine3D
 "`raylib/Vector3` `raylib/Vector3` `raylib/Color` -> ()"
 {:arglists '([startPos endPos color])}
 DrawLine3D
 [:raylib/Vector3 :raylib/Vector3 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawPoint3D
 "`raylib/Vector3` `raylib/Color` -> ()"
 {:arglists '([position color])}
 DrawPoint3D
 [:raylib/Vector3 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCircle3D
 "`raylib/Vector3` `float` `raylib/Vector3` `float` `raylib/Color` -> ()"
 {:arglists '([center radius rotationAxis rotationAngle color])}
 DrawCircle3D
 [:raylib/Vector3
  :coffi.mem/float
  :raylib/Vector3
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawTriangle3D
 "`raylib/Vector3` `raylib/Vector3` `raylib/Vector3` `raylib/Color` -> ()"
 {:arglists '([v1 v2 v3 color])}
 DrawTriangle3D
 [:raylib/Vector3 :raylib/Vector3 :raylib/Vector3 :raylib/Color]
 :coffi.mem/void)

(clojure.core/let
 [size-of-raylib__Vector3
  (coffi.mem/size-of :raylib/Vector3)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Vector3
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Vector3)
  alloc-raylib__Vector3-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc
    (clojure.core/* len size-of-raylib__Vector3)
    arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  DrawTriangleStrip3D
  "[`raylib/Vector3`] `raylib/Color` -> ()"
  {:arglists '([points color])}
  DrawTriangleStrip3D
  [:coffi.mem/pointer :coffi.mem/int :raylib/Color]
  :coffi.mem/void
  DrawTriangleStrip3D-native
  [points color]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [pointCount
     (clojure.core/count points)
     points'
     (clojure.core/let
      [local-segment (alloc-raylib__Vector3-list pointCount arena)]
      (clojure.core/loop
       [xs (clojure.core/seq points) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__Vector3
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__Vector3)))))
      local-segment)
     return-value-raw
     (DrawTriangleStrip3D-native points' pointCount color)
     return-value
     return-value-raw]
    nil))))

(coffi.ffi/defcfn
 DrawCube
 "`raylib/Vector3` `float` `float` `float` `raylib/Color` -> ()"
 {:arglists '([position width height length color])}
 DrawCube
 [:raylib/Vector3
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCubeV
 "`raylib/Vector3` `raylib/Vector3` `raylib/Color` -> ()"
 {:arglists '([position size color])}
 DrawCubeV
 [:raylib/Vector3 :raylib/Vector3 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCubeWires
 "`raylib/Vector3` `float` `float` `float` `raylib/Color` -> ()"
 {:arglists '([position width height length color])}
 DrawCubeWires
 [:raylib/Vector3
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCubeWiresV
 "`raylib/Vector3` `raylib/Vector3` `raylib/Color` -> ()"
 {:arglists '([position size color])}
 DrawCubeWiresV
 [:raylib/Vector3 :raylib/Vector3 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawSphere
 "`raylib/Vector3` `float` `raylib/Color` -> ()"
 {:arglists '([centerPos radius color])}
 DrawSphere
 [:raylib/Vector3 :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawSphereEx
 "`raylib/Vector3` `float` `int` `int` `raylib/Color` -> ()"
 {:arglists '([centerPos radius rings slices color])}
 DrawSphereEx
 [:raylib/Vector3
  :coffi.mem/float
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawSphereWires
 "`raylib/Vector3` `float` `int` `int` `raylib/Color` -> ()"
 {:arglists '([centerPos radius rings slices color])}
 DrawSphereWires
 [:raylib/Vector3
  :coffi.mem/float
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCylinder
 "`raylib/Vector3` `float` `float` `float` `int` `raylib/Color` -> ()"
 {:arglists '([position radiusTop radiusBottom height slices color])}
 DrawCylinder
 [:raylib/Vector3
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCylinderEx
 "`raylib/Vector3` `raylib/Vector3` `float` `float` `int` `raylib/Color` -> ()"
 {:arglists '([startPos endPos startRadius endRadius sides color])}
 DrawCylinderEx
 [:raylib/Vector3
  :raylib/Vector3
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCylinderWires
 "`raylib/Vector3` `float` `float` `float` `int` `raylib/Color` -> ()"
 {:arglists '([position radiusTop radiusBottom height slices color])}
 DrawCylinderWires
 [:raylib/Vector3
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCylinderWiresEx
 "`raylib/Vector3` `raylib/Vector3` `float` `float` `int` `raylib/Color` -> ()"
 {:arglists '([startPos endPos startRadius endRadius sides color])}
 DrawCylinderWiresEx
 [:raylib/Vector3
  :raylib/Vector3
  :coffi.mem/float
  :coffi.mem/float
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCapsule
 "`raylib/Vector3` `raylib/Vector3` `float` `int` `int` `raylib/Color` -> ()"
 {:arglists '([startPos endPos radius slices rings color])}
 DrawCapsule
 [:raylib/Vector3
  :raylib/Vector3
  :coffi.mem/float
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawCapsuleWires
 "`raylib/Vector3` `raylib/Vector3` `float` `int` `int` `raylib/Color` -> ()"
 {:arglists '([startPos endPos radius slices rings color])}
 DrawCapsuleWires
 [:raylib/Vector3
  :raylib/Vector3
  :coffi.mem/float
  :coffi.mem/int
  :coffi.mem/int
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawPlane
 "`raylib/Vector3` `raylib/Vector2` `raylib/Color` -> ()"
 {:arglists '([centerPos size color])}
 DrawPlane
 [:raylib/Vector3 :raylib/Vector2 :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawRay
 "`raylib/Ray` `raylib/Color` -> ()"
 {:arglists '([ray color])}
 DrawRay
 [:raylib/Ray :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawGrid
 "`int` `float` -> ()"
 {:arglists '([slices spacing])}
 DrawGrid
 [:coffi.mem/int :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 LoadModel
 "`string` -> `raylib/Model`"
 {:arglists '([fileName])}
 LoadModel
 [:coffi.mem/c-string]
 :raylib/Model)

(coffi.ffi/defcfn
 LoadModelFromMesh
 "`raylib/Mesh` -> `raylib/Model`"
 {:arglists '([mesh])}
 LoadModelFromMesh
 [:raylib/Mesh]
 :raylib/Model)

(coffi.ffi/defcfn
 IsModelReady
 "`raylib/Model` -> `coffimaker.runtime/bool`"
 {:arglists '([model])}
 IsModelReady
 [:raylib/Model]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 UnloadModel
 "`raylib/Model` -> ()"
 {:arglists '([model])}
 UnloadModel
 [:raylib/Model]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetModelBoundingBox
 "`raylib/Model` -> `raylib/BoundingBox`"
 {:arglists '([model])}
 GetModelBoundingBox
 [:raylib/Model]
 :raylib/BoundingBox)

(coffi.ffi/defcfn
 DrawModel
 "`raylib/Model` `raylib/Vector3` `float` `raylib/Color` -> ()"
 {:arglists '([model position scale tint])}
 DrawModel
 [:raylib/Model :raylib/Vector3 :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawModelEx
 "`raylib/Model` `raylib/Vector3` `raylib/Vector3` `float` `raylib/Vector3` `raylib/Color` -> ()"
 {:arglists '([model position rotationAxis rotationAngle scale tint])}
 DrawModelEx
 [:raylib/Model
  :raylib/Vector3
  :raylib/Vector3
  :coffi.mem/float
  :raylib/Vector3
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawModelWires
 "`raylib/Model` `raylib/Vector3` `float` `raylib/Color` -> ()"
 {:arglists '([model position scale tint])}
 DrawModelWires
 [:raylib/Model :raylib/Vector3 :coffi.mem/float :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawModelWiresEx
 "`raylib/Model` `raylib/Vector3` `raylib/Vector3` `float` `raylib/Vector3` `raylib/Color` -> ()"
 {:arglists '([model position rotationAxis rotationAngle scale tint])}
 DrawModelWiresEx
 [:raylib/Model
  :raylib/Vector3
  :raylib/Vector3
  :coffi.mem/float
  :raylib/Vector3
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawBoundingBox
 "`raylib/BoundingBox` `raylib/Color` -> ()"
 {:arglists '([box color])}
 DrawBoundingBox
 [:raylib/BoundingBox :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawBillboard
 "`raylib/Camera3D` `raylib/Texture` `raylib/Vector3` `float` `raylib/Color` -> ()"
 {:arglists '([camera texture position size tint])}
 DrawBillboard
 [:raylib/Camera3D
  :raylib/Texture
  :raylib/Vector3
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawBillboardRec
 "`raylib/Camera3D` `raylib/Texture` `raylib/Rectangle` `raylib/Vector3` `raylib/Vector2` `raylib/Color` -> ()"
 {:arglists '([camera texture source position size tint])}
 DrawBillboardRec
 [:raylib/Camera3D
  :raylib/Texture
  :raylib/Rectangle
  :raylib/Vector3
  :raylib/Vector2
  :raylib/Color]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawBillboardPro
 "`raylib/Camera3D` `raylib/Texture` `raylib/Rectangle` `raylib/Vector3` `raylib/Vector3` `raylib/Vector2` `raylib/Vector2` `float` `raylib/Color` -> ()"
 {:arglists
  '([camera texture source position up size origin rotation tint])}
 DrawBillboardPro
 [:raylib/Camera3D
  :raylib/Texture
  :raylib/Rectangle
  :raylib/Vector3
  :raylib/Vector3
  :raylib/Vector2
  :raylib/Vector2
  :coffi.mem/float
  :raylib/Color]
 :coffi.mem/void)

(clojure.core/let
 [size-of-raylib__Mesh
  (coffi.mem/size-of :raylib/Mesh)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Mesh
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Mesh)
  deserialize-from-raylib__Mesh
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Mesh)
  alloc-raylib__Mesh
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Mesh arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  UploadMesh
  "`raylib/Mesh` `coffimaker.runtime/bool` -> `raylib/Mesh`"
  {:arglists '([mesh dynamic])}
  UploadMesh
  [:coffi.mem/pointer :coffimaker.runtime/bool]
  :coffi.mem/void
  UploadMesh-native
  [mesh! dynamic]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [mesh'
     (clojure.core/let
      [local-segment (alloc-raylib__Mesh arena)]
      (serialize-into-raylib__Mesh mesh! nil local-segment nil)
      local-segment)
     return-value-raw
     (UploadMesh-native mesh' dynamic)
     mesh
     (deserialize-from-raylib__Mesh
      (.reinterpret
       ^java.lang.foreign.MemorySegment mesh'
       size-of-raylib__Mesh)
      nil)
     return-value
     return-value-raw]
    mesh))))

(coffi.ffi/defcfn
 UpdateMeshBuffer
 "`raylib/Mesh` `int` `pointer` `int` `int` -> ()"
 {:arglists '([mesh index data dataSize offset])}
 UpdateMeshBuffer
 [:raylib/Mesh
  :coffi.mem/int
  :coffi.mem/pointer
  :coffi.mem/int
  :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UnloadMesh
 "`raylib/Mesh` -> ()"
 {:arglists '([mesh])}
 UnloadMesh
 [:raylib/Mesh]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DrawMesh
 "`raylib/Mesh` `raylib/Material` `raylib/Matrix` -> ()"
 {:arglists '([mesh material transform])}
 DrawMesh
 [:raylib/Mesh :raylib/Material :raylib/Matrix]
 :coffi.mem/void)

(clojure.core/let
 [size-of-raylib__Matrix
  (coffi.mem/size-of :raylib/Matrix)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Matrix
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Matrix)
  alloc-raylib__Matrix-list
  (clojure.core/fn
   [len arena]
   (coffi.mem/alloc (clojure.core/* len size-of-raylib__Matrix) arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  DrawMeshInstanced
  "`raylib/Mesh` `raylib/Material` [`raylib/Matrix`] -> ()"
  {:arglists '([mesh material transforms])}
  DrawMeshInstanced
  [:raylib/Mesh :raylib/Material :coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void
  DrawMeshInstanced-native
  [mesh material transforms]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [instances
     (clojure.core/count transforms)
     transforms'
     (clojure.core/let
      [local-segment (alloc-raylib__Matrix-list instances arena)]
      (clojure.core/loop
       [xs (clojure.core/seq transforms) offset 0]
       (if
        xs
        (do
         (serialize-into-raylib__Matrix
          (clojure.core/first xs)
          nil
          local-segment
          nil)
         (recur
          (clojure.core/next xs)
          (clojure.core/+ offset size-of-raylib__Matrix)))))
      local-segment)
     return-value-raw
     (DrawMeshInstanced-native mesh material transforms' instances)
     return-value
     return-value-raw]
    nil))))

(coffi.ffi/defcfn
 ExportMesh
 "`raylib/Mesh` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([mesh fileName])}
 ExportMesh
 [:raylib/Mesh :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GetMeshBoundingBox
 "`raylib/Mesh` -> `raylib/BoundingBox`"
 {:arglists '([mesh])}
 GetMeshBoundingBox
 [:raylib/Mesh]
 :raylib/BoundingBox)

(clojure.core/let
 [size-of-raylib__Mesh
  (coffi.mem/size-of :raylib/Mesh)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Mesh
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Mesh)
  deserialize-from-raylib__Mesh
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Mesh)
  alloc-raylib__Mesh
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Mesh arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  GenMeshTangents
  "`raylib/Mesh` -> `raylib/Mesh`"
  {:arglists '([mesh])}
  GenMeshTangents
  [:coffi.mem/pointer]
  :coffi.mem/void
  GenMeshTangents-native
  [mesh!]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [mesh'
     (clojure.core/let
      [local-segment (alloc-raylib__Mesh arena)]
      (serialize-into-raylib__Mesh mesh! nil local-segment nil)
      local-segment)
     return-value-raw
     (GenMeshTangents-native mesh')
     mesh
     (deserialize-from-raylib__Mesh
      (.reinterpret
       ^java.lang.foreign.MemorySegment mesh'
       size-of-raylib__Mesh)
      nil)
     return-value
     return-value-raw]
    mesh))))

(coffi.ffi/defcfn
 GenMeshPoly
 "`int` `float` -> `raylib/Mesh`"
 {:arglists '([sides radius])}
 GenMeshPoly
 [:coffi.mem/int :coffi.mem/float]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshPlane
 "`float` `float` `int` `int` -> `raylib/Mesh`"
 {:arglists '([width length resX resZ])}
 GenMeshPlane
 [:coffi.mem/float :coffi.mem/float :coffi.mem/int :coffi.mem/int]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshCube
 "`float` `float` `float` -> `raylib/Mesh`"
 {:arglists '([width height length])}
 GenMeshCube
 [:coffi.mem/float :coffi.mem/float :coffi.mem/float]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshSphere
 "`float` `int` `int` -> `raylib/Mesh`"
 {:arglists '([radius rings slices])}
 GenMeshSphere
 [:coffi.mem/float :coffi.mem/int :coffi.mem/int]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshHemiSphere
 "`float` `int` `int` -> `raylib/Mesh`"
 {:arglists '([radius rings slices])}
 GenMeshHemiSphere
 [:coffi.mem/float :coffi.mem/int :coffi.mem/int]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshCylinder
 "`float` `float` `int` -> `raylib/Mesh`"
 {:arglists '([radius height slices])}
 GenMeshCylinder
 [:coffi.mem/float :coffi.mem/float :coffi.mem/int]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshCone
 "`float` `float` `int` -> `raylib/Mesh`"
 {:arglists '([radius height slices])}
 GenMeshCone
 [:coffi.mem/float :coffi.mem/float :coffi.mem/int]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshTorus
 "`float` `float` `int` `int` -> `raylib/Mesh`"
 {:arglists '([radius size radSeg sides])}
 GenMeshTorus
 [:coffi.mem/float :coffi.mem/float :coffi.mem/int :coffi.mem/int]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshKnot
 "`float` `float` `int` `int` -> `raylib/Mesh`"
 {:arglists '([radius size radSeg sides])}
 GenMeshKnot
 [:coffi.mem/float :coffi.mem/float :coffi.mem/int :coffi.mem/int]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshHeightmap
 "`raylib/Image` `raylib/Vector3` -> `raylib/Mesh`"
 {:arglists '([heightmap size])}
 GenMeshHeightmap
 [:raylib/Image :raylib/Vector3]
 :raylib/Mesh)

(coffi.ffi/defcfn
 GenMeshCubicmap
 "`raylib/Image` `raylib/Vector3` -> `raylib/Mesh`"
 {:arglists '([cubicmap cubeSize])}
 GenMeshCubicmap
 [:raylib/Image :raylib/Vector3]
 :raylib/Mesh)

(clojure.core/let
 [size-of-raylib__Material
  (coffi.mem/size-of :raylib/Material)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  deserialize-from-raylib__Material
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Material)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))]
 (coffi.ffi/defcfn
  LoadMaterials
  "`string` -> [`raylib/Material`]"
  {:arglists '([fileName])}
  LoadMaterials
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/pointer
  LoadMaterials-native
  [fileName]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [materialCount'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (LoadMaterials-native fileName materialCount')
     materialCount
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment materialCount' 4)
      0)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i materialCount)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (deserialize-from-raylib__Material
          (coffimaker.runtime/unsafe-offset
           (.reinterpret
            ^java.lang.foreign.MemorySegment return-value-raw
            (clojure.core/* materialCount size-of-raylib__Material))
           (clojure.core/* size-of-raylib__Material i))
          nil)))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(coffi.ffi/defcfn
 LoadMaterialDefault
 "() -> `raylib/Material`"
 {:arglists '([])}
 LoadMaterialDefault
 []
 :raylib/Material)

(coffi.ffi/defcfn
 IsMaterialReady
 "`raylib/Material` -> `coffimaker.runtime/bool`"
 {:arglists '([material])}
 IsMaterialReady
 [:raylib/Material]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 UnloadMaterial
 "`raylib/Material` -> ()"
 {:arglists '([material])}
 UnloadMaterial
 [:raylib/Material]
 :coffi.mem/void)

(clojure.core/let
 [size-of-raylib__Material
  (coffi.mem/size-of :raylib/Material)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Material
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Material)
  deserialize-from-raylib__Material
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Material)
  alloc-raylib__Material
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Material arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  SetMaterialTexture
  "`raylib/Material` `int` `raylib/Texture` -> `raylib/Material`"
  {:arglists '([material mapType texture])}
  SetMaterialTexture
  [:coffi.mem/pointer :coffi.mem/int :raylib/Texture]
  :coffi.mem/void
  SetMaterialTexture-native
  [material! mapType texture]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [material'
     (clojure.core/let
      [local-segment (alloc-raylib__Material arena)]
      (serialize-into-raylib__Material material! nil local-segment nil)
      local-segment)
     return-value-raw
     (SetMaterialTexture-native material' mapType texture)
     material
     (deserialize-from-raylib__Material
      (.reinterpret
       ^java.lang.foreign.MemorySegment material'
       size-of-raylib__Material)
      nil)
     return-value
     return-value-raw]
    material))))

(clojure.core/let
 [size-of-raylib__Model
  (coffi.mem/size-of :raylib/Model)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Model
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Model)
  deserialize-from-raylib__Model
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Model)
  alloc-raylib__Model
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Model arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  SetModelMeshMaterial
  "`raylib/Model` `int` `int` -> `raylib/Model`"
  {:arglists '([model meshId materialId])}
  SetModelMeshMaterial
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void
  SetModelMeshMaterial-native
  [model! meshId materialId]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [model'
     (clojure.core/let
      [local-segment (alloc-raylib__Model arena)]
      (serialize-into-raylib__Model model! nil local-segment nil)
      local-segment)
     return-value-raw
     (SetModelMeshMaterial-native model' meshId materialId)
     model
     (deserialize-from-raylib__Model
      (.reinterpret
       ^java.lang.foreign.MemorySegment model'
       size-of-raylib__Model)
      nil)
     return-value
     return-value-raw]
    model))))

(clojure.core/let
 [size-of-raylib__ModelAnimation
  (coffi.mem/size-of :raylib/ModelAnimation)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  size-of-coffi_mem__int
  (coffi.mem/size-of :coffi.mem/int)
  deserialize-from-raylib__ModelAnimation
  (clojure.core/get-method
   coffi.mem/deserialize-from
   :raylib/ModelAnimation)
  deserialize-from-coffi_mem__int
  (clojure.core/get-method coffi.mem/deserialize-from :coffi.mem/int)
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))
  alloc-coffi_mem__int
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__int arena))]
 (coffi.ffi/defcfn
  LoadModelAnimations
  "`string` -> [`raylib/ModelAnimation`]"
  {:arglists '([fileName])}
  LoadModelAnimations
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/pointer
  LoadModelAnimations-native
  [fileName]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [animCount'
     (alloc-coffi_mem__int arena)
     return-value-raw
     (LoadModelAnimations-native fileName animCount')
     animCount
     (coffi.mem/read-int
      (.reinterpret ^java.lang.foreign.MemorySegment animCount' 4)
      0)
     return-value
     (clojure.core/loop
      [i 0 v (clojure.core/transient [])]
      (if
       (clojure.core/< i animCount)
       (recur
        (clojure.core/unchecked-inc i)
        (clojure.core/conj!
         v
         (deserialize-from-raylib__ModelAnimation
          (coffimaker.runtime/unsafe-offset
           (.reinterpret
            ^java.lang.foreign.MemorySegment return-value-raw
            (clojure.core/* animCount size-of-raylib__ModelAnimation))
           (clojure.core/* size-of-raylib__ModelAnimation i))
          nil)))
       (clojure.core/persistent! v)))]
    {:return-value return-value,
     :return-value-ptr return-value-raw}))))

(coffi.ffi/defcfn
 UpdateModelAnimation
 "`raylib/Model` `raylib/ModelAnimation` `int` -> ()"
 {:arglists '([model anim frame])}
 UpdateModelAnimation
 [:raylib/Model :raylib/ModelAnimation :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UnloadModelAnimation
 "`raylib/ModelAnimation` -> ()"
 {:arglists '([anim])}
 UnloadModelAnimation
 [:raylib/ModelAnimation]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UnloadModelAnimations
 "*`raylib/ModelAnimation` `int` -> ()"
 {:arglists '([animations count])}
 UnloadModelAnimations
 [:coffi.mem/pointer :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsModelAnimationValid
 "`raylib/Model` `raylib/ModelAnimation` -> `coffimaker.runtime/bool`"
 {:arglists '([model anim])}
 IsModelAnimationValid
 [:raylib/Model :raylib/ModelAnimation]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 CheckCollisionSpheres
 "`raylib/Vector3` `float` `raylib/Vector3` `float` -> `coffimaker.runtime/bool`"
 {:arglists '([center1 radius1 center2 radius2])}
 CheckCollisionSpheres
 [:raylib/Vector3 :coffi.mem/float :raylib/Vector3 :coffi.mem/float]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 CheckCollisionBoxes
 "`raylib/BoundingBox` `raylib/BoundingBox` -> `coffimaker.runtime/bool`"
 {:arglists '([box1 box2])}
 CheckCollisionBoxes
 [:raylib/BoundingBox :raylib/BoundingBox]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 CheckCollisionBoxSphere
 "`raylib/BoundingBox` `raylib/Vector3` `float` -> `coffimaker.runtime/bool`"
 {:arglists '([box center radius])}
 CheckCollisionBoxSphere
 [:raylib/BoundingBox :raylib/Vector3 :coffi.mem/float]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 GetRayCollisionSphere
 "`raylib/Ray` `raylib/Vector3` `float` -> `raylib/RayCollision`"
 {:arglists '([ray center radius])}
 GetRayCollisionSphere
 [:raylib/Ray :raylib/Vector3 :coffi.mem/float]
 :raylib/RayCollision)

(coffi.ffi/defcfn
 GetRayCollisionBox
 "`raylib/Ray` `raylib/BoundingBox` -> `raylib/RayCollision`"
 {:arglists '([ray box])}
 GetRayCollisionBox
 [:raylib/Ray :raylib/BoundingBox]
 :raylib/RayCollision)

(coffi.ffi/defcfn
 GetRayCollisionMesh
 "`raylib/Ray` `raylib/Mesh` `raylib/Matrix` -> `raylib/RayCollision`"
 {:arglists '([ray mesh transform])}
 GetRayCollisionMesh
 [:raylib/Ray :raylib/Mesh :raylib/Matrix]
 :raylib/RayCollision)

(coffi.ffi/defcfn
 GetRayCollisionTriangle
 "`raylib/Ray` `raylib/Vector3` `raylib/Vector3` `raylib/Vector3` -> `raylib/RayCollision`"
 {:arglists '([ray p1 p2 p3])}
 GetRayCollisionTriangle
 [:raylib/Ray :raylib/Vector3 :raylib/Vector3 :raylib/Vector3]
 :raylib/RayCollision)

(coffi.ffi/defcfn
 GetRayCollisionQuad
 "`raylib/Ray` `raylib/Vector3` `raylib/Vector3` `raylib/Vector3` `raylib/Vector3` -> `raylib/RayCollision`"
 {:arglists '([ray p1 p2 p3 p4])}
 GetRayCollisionQuad
 [:raylib/Ray
  :raylib/Vector3
  :raylib/Vector3
  :raylib/Vector3
  :raylib/Vector3]
 :raylib/RayCollision)

(coffi.mem/defalias
 :raylib/AudioCallback
 [:coffi.ffi/fn [:coffi.mem/pointer :coffi.mem/int] :coffi.mem/void])

(coffi.ffi/defcfn
 InitAudioDevice
 "() -> ()"
 {:arglists '([])}
 InitAudioDevice
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 CloseAudioDevice
 "() -> ()"
 {:arglists '([])}
 CloseAudioDevice
 []
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsAudioDeviceReady
 "() -> `coffimaker.runtime/bool`"
 {:arglists '([])}
 IsAudioDeviceReady
 []
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 SetMasterVolume
 "`float` -> ()"
 {:arglists '([volume])}
 SetMasterVolume
 [:coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 LoadWave
 "`string` -> `raylib/Wave`"
 {:arglists '([fileName])}
 LoadWave
 [:coffi.mem/c-string]
 :raylib/Wave)

(coffi.ffi/defcfn
 LoadWaveFromMemory
 "`string` `string` `int` -> `raylib/Wave`"
 {:arglists '([fileType fileData dataSize])}
 LoadWaveFromMemory
 [:coffi.mem/c-string :coffi.mem/c-string :coffi.mem/int]
 :raylib/Wave)

(coffi.ffi/defcfn
 IsWaveReady
 "`raylib/Wave` -> `coffimaker.runtime/bool`"
 {:arglists '([wave])}
 IsWaveReady
 [:raylib/Wave]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 LoadSound
 "`string` -> `raylib/Sound`"
 {:arglists '([fileName])}
 LoadSound
 [:coffi.mem/c-string]
 :raylib/Sound)

(coffi.ffi/defcfn
 LoadSoundFromWave
 "`raylib/Wave` -> `raylib/Sound`"
 {:arglists '([wave])}
 LoadSoundFromWave
 [:raylib/Wave]
 :raylib/Sound)

(coffi.ffi/defcfn
 LoadSoundAlias
 "`raylib/Sound` -> `raylib/Sound`"
 {:arglists '([source])}
 LoadSoundAlias
 [:raylib/Sound]
 :raylib/Sound)

(coffi.ffi/defcfn
 IsSoundReady
 "`raylib/Sound` -> `coffimaker.runtime/bool`"
 {:arglists '([sound])}
 IsSoundReady
 [:raylib/Sound]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 UpdateSound
 "`raylib/Sound` `pointer` `int` -> ()"
 {:arglists '([sound data sampleCount])}
 UpdateSound
 [:raylib/Sound :coffi.mem/pointer :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UnloadWave
 "`raylib/Wave` -> ()"
 {:arglists '([wave])}
 UnloadWave
 [:raylib/Wave]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UnloadSound
 "`raylib/Sound` -> ()"
 {:arglists '([sound])}
 UnloadSound
 [:raylib/Sound]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UnloadSoundAlias
 "`raylib/Sound` -> ()"
 {:arglists '([alias])}
 UnloadSoundAlias
 [:raylib/Sound]
 :coffi.mem/void)

(coffi.ffi/defcfn
 ExportWave
 "`raylib/Wave` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([wave fileName])}
 ExportWave
 [:raylib/Wave :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 ExportWaveAsCode
 "`raylib/Wave` `string` -> `coffimaker.runtime/bool`"
 {:arglists '([wave fileName])}
 ExportWaveAsCode
 [:raylib/Wave :coffi.mem/c-string]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 PlaySound
 "`raylib/Sound` -> ()"
 {:arglists '([sound])}
 PlaySound
 [:raylib/Sound]
 :coffi.mem/void)

(coffi.ffi/defcfn
 StopSound
 "`raylib/Sound` -> ()"
 {:arglists '([sound])}
 StopSound
 [:raylib/Sound]
 :coffi.mem/void)

(coffi.ffi/defcfn
 PauseSound
 "`raylib/Sound` -> ()"
 {:arglists '([sound])}
 PauseSound
 [:raylib/Sound]
 :coffi.mem/void)

(coffi.ffi/defcfn
 ResumeSound
 "`raylib/Sound` -> ()"
 {:arglists '([sound])}
 ResumeSound
 [:raylib/Sound]
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsSoundPlaying
 "`raylib/Sound` -> `coffimaker.runtime/bool`"
 {:arglists '([sound])}
 IsSoundPlaying
 [:raylib/Sound]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 SetSoundVolume
 "`raylib/Sound` `float` -> ()"
 {:arglists '([sound volume])}
 SetSoundVolume
 [:raylib/Sound :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetSoundPitch
 "`raylib/Sound` `float` -> ()"
 {:arglists '([sound pitch])}
 SetSoundPitch
 [:raylib/Sound :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetSoundPan
 "`raylib/Sound` `float` -> ()"
 {:arglists '([sound pan])}
 SetSoundPan
 [:raylib/Sound :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 WaveCopy
 "`raylib/Wave` -> `raylib/Wave`"
 {:arglists '([wave])}
 WaveCopy
 [:raylib/Wave]
 :raylib/Wave)

(clojure.core/let
 [size-of-raylib__Wave
  (coffi.mem/size-of :raylib/Wave)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Wave
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Wave)
  deserialize-from-raylib__Wave
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Wave)
  alloc-raylib__Wave
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Wave arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  WaveCrop
  "`raylib/Wave` `int` `int` -> `raylib/Wave`"
  {:arglists '([wave initSample finalSample])}
  WaveCrop
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void
  WaveCrop-native
  [wave! initSample finalSample]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [wave'
     (clojure.core/let
      [local-segment (alloc-raylib__Wave arena)]
      (serialize-into-raylib__Wave wave! nil local-segment nil)
      local-segment)
     return-value-raw
     (WaveCrop-native wave' initSample finalSample)
     wave
     (deserialize-from-raylib__Wave
      (.reinterpret
       ^java.lang.foreign.MemorySegment wave'
       size-of-raylib__Wave)
      nil)
     return-value
     return-value-raw]
    wave))))

(clojure.core/let
 [size-of-raylib__Wave
  (coffi.mem/size-of :raylib/Wave)
  size-of-coffi_mem__pointer
  (coffi.mem/size-of :coffi.mem/pointer)
  serialize-into-raylib__Wave
  (clojure.core/get-method coffi.mem/serialize-into :raylib/Wave)
  deserialize-from-raylib__Wave
  (clojure.core/get-method coffi.mem/deserialize-from :raylib/Wave)
  alloc-raylib__Wave
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-raylib__Wave arena))
  alloc-coffi_mem__pointer
  (clojure.core/fn
   [arena]
   (coffi.mem/alloc size-of-coffi_mem__pointer arena))]
 (coffi.ffi/defcfn
  WaveFormat
  "`raylib/Wave` `int` `int` `int` -> `raylib/Wave`"
  {:arglists '([wave sampleRate sampleSize channels])}
  WaveFormat
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void
  WaveFormat-native
  [wave! sampleRate sampleSize channels]
  (clojure.core/with-open
   [arena (coffi.mem/thread-local-arena)]
   (clojure.core/let
    [wave'
     (clojure.core/let
      [local-segment (alloc-raylib__Wave arena)]
      (serialize-into-raylib__Wave wave! nil local-segment nil)
      local-segment)
     return-value-raw
     (WaveFormat-native wave' sampleRate sampleSize channels)
     wave
     (deserialize-from-raylib__Wave
      (.reinterpret
       ^java.lang.foreign.MemorySegment wave'
       size-of-raylib__Wave)
      nil)
     return-value
     return-value-raw]
    wave))))

(coffi.ffi/defcfn
 LoadWaveSamples
 "`raylib/Wave` -> *`float`"
 {:arglists '([wave])}
 LoadWaveSamples
 [:raylib/Wave]
 :coffi.mem/pointer)

(coffi.ffi/defcfn
 UnloadWaveSamples
 "*`float` -> ()"
 {:arglists '([samples])}
 UnloadWaveSamples
 [:coffi.mem/pointer]
 :coffi.mem/void)

(coffi.ffi/defcfn
 LoadMusicStream
 "`string` -> `raylib/Music`"
 {:arglists '([fileName])}
 LoadMusicStream
 [:coffi.mem/c-string]
 :raylib/Music)

(coffi.ffi/defcfn
 LoadMusicStreamFromMemory
 "`string` `string` `int` -> `raylib/Music`"
 {:arglists '([fileType data dataSize])}
 LoadMusicStreamFromMemory
 [:coffi.mem/c-string :coffi.mem/c-string :coffi.mem/int]
 :raylib/Music)

(coffi.ffi/defcfn
 IsMusicReady
 "`raylib/Music` -> `coffimaker.runtime/bool`"
 {:arglists '([music])}
 IsMusicReady
 [:raylib/Music]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 UnloadMusicStream
 "`raylib/Music` -> ()"
 {:arglists '([music])}
 UnloadMusicStream
 [:raylib/Music]
 :coffi.mem/void)

(coffi.ffi/defcfn
 PlayMusicStream
 "`raylib/Music` -> ()"
 {:arglists '([music])}
 PlayMusicStream
 [:raylib/Music]
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsMusicStreamPlaying
 "`raylib/Music` -> `coffimaker.runtime/bool`"
 {:arglists '([music])}
 IsMusicStreamPlaying
 [:raylib/Music]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 UpdateMusicStream
 "`raylib/Music` -> ()"
 {:arglists '([music])}
 UpdateMusicStream
 [:raylib/Music]
 :coffi.mem/void)

(coffi.ffi/defcfn
 StopMusicStream
 "`raylib/Music` -> ()"
 {:arglists '([music])}
 StopMusicStream
 [:raylib/Music]
 :coffi.mem/void)

(coffi.ffi/defcfn
 PauseMusicStream
 "`raylib/Music` -> ()"
 {:arglists '([music])}
 PauseMusicStream
 [:raylib/Music]
 :coffi.mem/void)

(coffi.ffi/defcfn
 ResumeMusicStream
 "`raylib/Music` -> ()"
 {:arglists '([music])}
 ResumeMusicStream
 [:raylib/Music]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SeekMusicStream
 "`raylib/Music` `float` -> ()"
 {:arglists '([music position])}
 SeekMusicStream
 [:raylib/Music :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetMusicVolume
 "`raylib/Music` `float` -> ()"
 {:arglists '([music volume])}
 SetMusicVolume
 [:raylib/Music :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetMusicPitch
 "`raylib/Music` `float` -> ()"
 {:arglists '([music pitch])}
 SetMusicPitch
 [:raylib/Music :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetMusicPan
 "`raylib/Music` `float` -> ()"
 {:arglists '([music pan])}
 SetMusicPan
 [:raylib/Music :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 GetMusicTimeLength
 "`raylib/Music` -> `float`"
 {:arglists '([music])}
 GetMusicTimeLength
 [:raylib/Music]
 :coffi.mem/float)

(coffi.ffi/defcfn
 GetMusicTimePlayed
 "`raylib/Music` -> `float`"
 {:arglists '([music])}
 GetMusicTimePlayed
 [:raylib/Music]
 :coffi.mem/float)

(coffi.ffi/defcfn
 LoadAudioStream
 "`int` `int` `int` -> `raylib/AudioStream`"
 {:arglists '([sampleRate sampleSize channels])}
 LoadAudioStream
 [:coffi.mem/int :coffi.mem/int :coffi.mem/int]
 :raylib/AudioStream)

(coffi.ffi/defcfn
 IsAudioStreamReady
 "`raylib/AudioStream` -> `coffimaker.runtime/bool`"
 {:arglists '([stream])}
 IsAudioStreamReady
 [:raylib/AudioStream]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 UnloadAudioStream
 "`raylib/AudioStream` -> ()"
 {:arglists '([stream])}
 UnloadAudioStream
 [:raylib/AudioStream]
 :coffi.mem/void)

(coffi.ffi/defcfn
 UpdateAudioStream
 "`raylib/AudioStream` `pointer` `int` -> ()"
 {:arglists '([stream data frameCount])}
 UpdateAudioStream
 [:raylib/AudioStream :coffi.mem/pointer :coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsAudioStreamProcessed
 "`raylib/AudioStream` -> `coffimaker.runtime/bool`"
 {:arglists '([stream])}
 IsAudioStreamProcessed
 [:raylib/AudioStream]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 PlayAudioStream
 "`raylib/AudioStream` -> ()"
 {:arglists '([stream])}
 PlayAudioStream
 [:raylib/AudioStream]
 :coffi.mem/void)

(coffi.ffi/defcfn
 PauseAudioStream
 "`raylib/AudioStream` -> ()"
 {:arglists '([stream])}
 PauseAudioStream
 [:raylib/AudioStream]
 :coffi.mem/void)

(coffi.ffi/defcfn
 ResumeAudioStream
 "`raylib/AudioStream` -> ()"
 {:arglists '([stream])}
 ResumeAudioStream
 [:raylib/AudioStream]
 :coffi.mem/void)

(coffi.ffi/defcfn
 IsAudioStreamPlaying
 "`raylib/AudioStream` -> `coffimaker.runtime/bool`"
 {:arglists '([stream])}
 IsAudioStreamPlaying
 [:raylib/AudioStream]
 :coffimaker.runtime/bool)

(coffi.ffi/defcfn
 StopAudioStream
 "`raylib/AudioStream` -> ()"
 {:arglists '([stream])}
 StopAudioStream
 [:raylib/AudioStream]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetAudioStreamVolume
 "`raylib/AudioStream` `float` -> ()"
 {:arglists '([stream volume])}
 SetAudioStreamVolume
 [:raylib/AudioStream :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetAudioStreamPitch
 "`raylib/AudioStream` `float` -> ()"
 {:arglists '([stream pitch])}
 SetAudioStreamPitch
 [:raylib/AudioStream :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetAudioStreamPan
 "`raylib/AudioStream` `float` -> ()"
 {:arglists '([stream pan])}
 SetAudioStreamPan
 [:raylib/AudioStream :coffi.mem/float]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetAudioStreamBufferSizeDefault
 "`int` -> ()"
 {:arglists '([size])}
 SetAudioStreamBufferSizeDefault
 [:coffi.mem/int]
 :coffi.mem/void)

(coffi.ffi/defcfn
 SetAudioStreamCallback
 "`raylib/AudioStream` (`pointer` `int` -> ()) -> ()"
 {:arglists '([stream callback])}
 SetAudioStreamCallback
 [:raylib/AudioStream
  [:coffi.ffi/fn [:coffi.mem/pointer :coffi.mem/int] :coffi.mem/void]]
 :coffi.mem/void)

(coffi.ffi/defcfn
 AttachAudioStreamProcessor
 "`raylib/AudioStream` (`pointer` `int` -> ()) -> ()"
 {:arglists '([stream processor])}
 AttachAudioStreamProcessor
 [:raylib/AudioStream
  [:coffi.ffi/fn [:coffi.mem/pointer :coffi.mem/int] :coffi.mem/void]]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DetachAudioStreamProcessor
 "`raylib/AudioStream` (`pointer` `int` -> ()) -> ()"
 {:arglists '([stream processor])}
 DetachAudioStreamProcessor
 [:raylib/AudioStream
  [:coffi.ffi/fn [:coffi.mem/pointer :coffi.mem/int] :coffi.mem/void]]
 :coffi.mem/void)

(coffi.ffi/defcfn
 AttachAudioMixedProcessor
 "(`pointer` `int` -> ()) -> ()"
 {:arglists '([processor])}
 AttachAudioMixedProcessor
 [[:coffi.ffi/fn [:coffi.mem/pointer :coffi.mem/int] :coffi.mem/void]]
 :coffi.mem/void)

(coffi.ffi/defcfn
 DetachAudioMixedProcessor
 "(`pointer` `int` -> ()) -> ()"
 {:arglists '([processor])}
 DetachAudioMixedProcessor
 [[:coffi.ffi/fn [:coffi.mem/pointer :coffi.mem/int] :coffi.mem/void]]
 :coffi.mem/void)

(def ^{:const true} WIN32 1)

(def ^{:const true} WINNT 1)

(def ^{:const true} WIN64 1)

(def ^{:const true} RAYLIB_H "")

(def ^{:const true} RAYLIB_VERSION_MAJOR 4)

(def ^{:const true} RAYLIB_VERSION_MINOR 6)

(def ^{:const true} RAYLIB_VERSION_PATCH 0)

(def ^{:const true} RAYLIB_VERSION "4.6-dev")

(def ^{:const true} RLAPI "")

(def ^{:const true} PI 3.1415927)

(def ^{:const true} DEG2RAD 0.017453292)

(def ^{:const true} RAD2DEG 57.295776)

(def ^{:const true} RL_COLOR_TYPE "")

(def ^{:const true} RL_RECTANGLE_TYPE "")

(def ^{:const true} RL_VECTOR2_TYPE "")

(def ^{:const true} RL_VECTOR3_TYPE "")

(def ^{:const true} RL_VECTOR4_TYPE "")

(def ^{:const true} RL_QUATERNION_TYPE "")

(def ^{:const true} RL_MATRIX_TYPE "")

(def LIGHTGRAY (Color. 200 200 200 255))

(def GRAY (Color. 130 130 130 255))

(def DARKGRAY (Color. 80 80 80 255))

(def YELLOW (Color. 253 249 0 255))

(def GOLD (Color. 255 203 0 255))

(def ORANGE (Color. 255 161 0 255))

(def PINK (Color. 255 109 194 255))

(def RED (Color. 230 41 55 255))

(def MAROON (Color. 190 33 55 255))

(def GREEN (Color. 0 228 48 255))

(def LIME (Color. 0 158 47 255))

(def DARKGREEN (Color. 0 117 44 255))

(def SKYBLUE (Color. 102 191 255 255))

(def BLUE (Color. 0 121 241 255))

(def DARKBLUE (Color. 0 82 172 255))

(def PURPLE (Color. 200 122 255 255))

(def VIOLET (Color. 135 60 190 255))

(def DARKPURPLE (Color. 112 31 126 255))

(def BEIGE (Color. 211 176 131 255))

(def BROWN (Color. 127 106 79 255))

(def DARKBROWN (Color. 76 63 47 255))

(def WHITE (Color. 255 255 255 255))

(def BLACK (Color. 0 0 0 255))

(def BLANK (Color. 0 0 0 0))

(def MAGENTA (Color. 255 0 255 255))

(def RAYWHITE (Color. 245 245 245 255))

(coffi.mem/defalias :raylib/bool :coffimaker.runtime/bool)

(def ^{:const true} MOUSE_LEFT_BUTTON 0)

(def ^{:const true} MOUSE_RIGHT_BUTTON 1)

(def ^{:const true} MOUSE_MIDDLE_BUTTON 2)

(def ^{:const true} MATERIAL_MAP_DIFFUSE 0)

(def ^{:const true} MATERIAL_MAP_SPECULAR 1)

(def ^{:const true} SHADER_LOC_MAP_DIFFUSE 15)

(def ^{:const true} SHADER_LOC_MAP_SPECULAR 16)
