(ns raylib-clj.core
  (:gen-class)
  (:require
   [tech.v3.datatype :as dtype]
   [tech.v3.datatype.ffi :as ffi]
   [tech.v3.datatype.ffi.size-t :as ffi.size-t]
   [tech.v3.datatype.struct :as dt-struct]
   [coffi.mem :as mem]
   [coffi.ffi :as cffi]
   [coffi.layout :as layout]
   [camel-snake-kebab.core :as csk]
   ))

(defmacro def- [name & decls]
  (list* `def (with-meta name (assoc (meta name) :private true)) decls))

(defmacro defconst [name & decls]
  (list* `def (with-meta name (assoc (meta name) :const true)) decls))

(defn- f32     [name] [name ::mem/float])
(defn- uchar   [name] [name ::mem/char])
(defn- i8      [name] [name ::mem/byte])
(defn- i16     [name] [name ::mem/short])
(defn- i32     [name] [name ::mem/int])
(defn- ui8     [name] [name ::mem/byte])
(defn- ui16    [name] [name ::mem/short])
(defn- ui32    [name] [name ::mem/int])
(defn- bool    [name] [name ::mem/byte])
(defn- pointer [name] [name ::mem/pointer])
(def- ptr pointer)

(defn- struct->int8-array
  ([decl-name decl-type]
   {:name (keyword decl-name)
    :datatype :int8
    :n-elems (dt-struct/datatype-size (keyword decl-type))})
  ([decl-type]
   {:datatype :int8
    :n-elems (dt-struct/datatype-size (keyword decl-type))}))

(defmacro define-datatype! [type-name members]
  `(mem/defalias ~(keyword "raylib-clj.core" (name type-name))
     (layout/with-c-layout
       [::mem/struct ~members])))

(define-datatype! :vec2 [(f32 :x) (f32 :y)])
(define-datatype! :vec3 [(f32 :x) (f32 :y) (f32 :z)])
(define-datatype! :vec4 [(f32 :x) (f32 :y) (f32 :z) (f32 :w)])

;alias quaternion as vec4?

(define-datatype! :mat4
  [(f32 :m0)(f32 :m4)(f32 :m8) (f32 :m12)
   (f32 :m1)(f32 :m5)(f32 :m9) (f32 :m13)
   (f32 :m2)(f32 :m6)(f32 :m10)(f32 :m14)
   (f32 :m3)(f32 :m7)(f32 :m11)(f32 :m15)])

(define-datatype! :color [(uchar :r) (uchar :g) (uchar :b) (uchar :a)])

(define-datatype! :rectangle [(f32 :x) (f32 :y) (f32 :width) (f32 :height)])

(define-datatype! :image
  [(pointer :data) (i32 :width) (i32 :height) (i32 :mipmaps) (i32 :format)])

(define-datatype! :texture
  [(ui32 :id) (i32 :width) (i32 :height) (i32 :mipmaps) (i32 :format)])

;alias texture-2d as texture?

;alias texture-cubemap as texture?

(define-datatype! :render-texture
  [(ui32 :id)
   [:texture ::texture]
   [:depth ::texture]
   ])

;alias render-texture-2d as render-texture?

(define-datatype! :n-patch-info
  [[:source ::rectangle]
   (i32 :left) (i32 :top) (i32 :right) (i32 :bottom) (i32 :layout)])

(define-datatype! :glyph-info
  [(i32 :value) (i32 :offset-x) (i32 :offset-y) (i32 :advance-x)
   [:image ::image]])

(define-datatype! :font
  [(i32 :base-size) (i32 :glyph-count) (i32 :glyph-padding)
   [:texture ::texture]
   (ptr :rectangles) ;pointer to rectangle
   (ptr :glyphs) ;pointer to glyph
   ])

(define-datatype! :camera-3d
  [[:position ::vec3]
   [:target ::vec3]
   [:up ::vec3]
   (f32 :fovy)
   (i32 :projection)])

;typedef :camera-3d Camera;    // :camera-3d type fallback, defaults to Camera3D

(define-datatype! :camera-2d
  [[:position ::vec2]
   [:target ::vec2]
   (f32 :rotation)
   (f32 :zoom)])

(define-datatype! :mesh
  [(i32 :vertex-count)
   (i32 :triangle-count)
   (ptr :vertices)
   (ptr :texcoords)
   (ptr :texcoords-2)
   (ptr :normals)
   (ptr :tangents)
   (ptr :colors)
   (ptr :indices)
   (ptr :animation-vertices)
   (ptr :animation-normals)
   (ptr :bone-ids)
   (ptr :bone-weights)
   (ui32 :vao-id)
   (ptr :vbo-id)])

(define-datatype! :shader
  [(ui32 :id)
   (ptr :locs)])

(define-datatype! :material-map
  [[:texture ::texture]
   [:color ::color]
   (f32 :value)])

(define-datatype! :material
  [[:shader ::shader]
   (ptr :maps)
   [:params [::mem/array ::mem/float 4]]])

(define-datatype! :transform
  [[:translation ::vec3]
   [:rotation ::vec4]
   [:scale ::vec3]])

(define-datatype! :bone-info
  [[:name [::mem/array ::mem/char 32]]
   (i32 :parent)])

(define-datatype! :model
  [[:transform ::mat4]
   (i32 :mesh-count)
   (i32 :material-count)
   (ptr :meshes)
   (ptr :materials)
   (ptr :mesh-material)
   (i32 :bone-count)
   (ptr :bones)
   (ptr :bind-pose)])

(define-datatype! :model-animation
  [(i32 :bone-count)
   (i32 :frame-count)
   (ptr :bones)
   (ptr :frame-poses) ;type: Transform**
   [:name [::mem/array ::mem/char 32]]])

(define-datatype! :ray
  [[:position ::vec3]
   [:direction ::vec3]])

(define-datatype! :ray-collision
  [(bool :hit)
   (f32 :distance)
   [:point ::vec3]
   [:normal ::vec3]])

(define-datatype! :bounding-box
  [[:min ::vec3]
   [:max ::vec3]])

(define-datatype! :wave
  [(ui32 :frame-count)
   (ui32 :sample-rate)
   (ui32 :sample-size)
   (ui32 :channels)
   (ptr :data)])

;// Opaque structs declaration
;// NOTE: Actual structs are defined internally in raudio module
;typedef struct rAudioBuffer rAudioBuffer;
;typedef struct rAudioProcessor rAudioProcessor;

(define-datatype! :audio-stream
  [(ptr :buffer)
   (ptr :processor)
   (ui32 :sample-rate)
   (ui32 :sample-size)
   (ui32 :channels)])

(define-datatype! :sound
  [[:stream ::audio-stream]
   (ui32 :frame-count)])

(define-datatype! :music
  [[:stream ::audio-stream]
   (ui32 :frame-count)
   (bool :looping)
   (i32 :context-type)
   (ptr :context-data)])

(define-datatype! :vr-device-info
  [(i32 :horizontal-resolution)
   (i32 :vertical-resolution)
   (f32 :horizontal-screen-size)
   (f32 :vertical-screen-size)
   (f32 :vertical-screen-center)
   (f32 :lens-separation-distance)
   (f32 :interpupillary-distance)
   [:lens-distortion-values [::mem/array ::mem/float 4]]
   [:chromatic-aberration-correction-values [::mem/array ::mem/float 4]]])

(define-datatype! :vr-stereo-config
  [[:projection          [::mem/array ::mat4 2]]
   [:view-offset         [::mem/array ::mat4 2]]
   [:left-lens-center    [::mem/array ::mem/float 2]]
   [:right-lens-center   [::mem/array ::mem/float 2]]
   [:left-screen-center  [::mem/array ::mem/float 2]]
   [:right-screen-center [::mem/array ::mem/float 2]]
   [:scale               [::mem/array ::mem/float 2]]
   [:scale-in            [::mem/array ::mem/float 2]]])

(define-datatype! :file-path-list
  [(ui32 :capacity)
   (ui32 :count)
   (ptr :paths)])
; enums

;typedef enum {
(defconst FLAG_VSYNC_HINT               0x00000040)
(defconst FLAG_FULLSCREEN_MODE          0x00000002)
(defconst FLAG_WINDOW_RESIZABLE         0x00000004)
(defconst FLAG_WINDOW_UNDECORATED       0x00000008)
(defconst FLAG_WINDOW_HIDDEN            0x00000080)
(defconst FLAG_WINDOW_MINIMIZED         0x00000200)
(defconst FLAG_WINDOW_MAXIMIZED         0x00000400)
(defconst FLAG_WINDOW_UNFOCUSED         0x00000800)
(defconst FLAG_WINDOW_TOPMOST           0x00001000)
(defconst FLAG_WINDOW_ALWAYS_RUN        0x00000100)
(defconst FLAG_WINDOW_TRANSPARENT       0x00000010)
(defconst FLAG_WINDOW_HIGHDPI           0x00002000)
(defconst FLAG_WINDOW_MOUSE_PASSTHROUGH 0x00004000)
(defconst FLAG_BORDERLESS_WINDOWED_MODE 0x00008000)
(defconst FLAG_MSAA_4X_HINT             0x00000020)
(defconst FLAG_INTERLACED_HINT          0x00010000)
;} ConfigFlags;

;// NOTE: Organized by priority level
;typedef enum {
(defconst LOG_ALL 0)
(defconst LOG_TRACE)
(defconst LOG_DEBUG)
(defconst LOG_INFO)
(defconst LOG_WARNING)
(defconst LOG_ERROR)
(defconst LOG_FATAL)
(defconst LOG_NONE)
;} TraceLogLevel;

;// Keyboard keys (US keyboard layout)
;// NOTE: Use GetKeyPressed() to allow redefining
;// required keys for alternative layouts
;typedef enum {
(defconst KEY_NULL 0)
;    // Alphanumeric keys
(defconst KEY_APOSTROPHE 39)
(defconst KEY_COMMA 44)
(defconst KEY_MINUS 45)
(defconst KEY_PERIOD 46)
(defconst KEY_SLASH 47)
(defconst KEY_ZERO 48)
(defconst KEY_ONE 49)
(defconst KEY_TWO 50)
(defconst KEY_THREE 51)
(defconst KEY_FOUR 52)
(defconst KEY_FIVE 53)
(defconst KEY_SIX 54)
(defconst KEY_SEVEN 55)
(defconst KEY_EIGHT 56)
(defconst KEY_NINE 57)
(defconst KEY_SEMICOLON 59)
(defconst KEY_EQUAL 61)
(defconst KEY_A 65)
(defconst KEY_B 66)
(defconst KEY_C 67)
(defconst KEY_D 68)
(defconst KEY_E 69)
(defconst KEY_F 70)
(defconst KEY_G 71)
(defconst KEY_H 72)
(defconst KEY_I 73)
(defconst KEY_J 74)
(defconst KEY_K 75)
(defconst KEY_L 76)
(defconst KEY_M 77)
(defconst KEY_N 78)
(defconst KEY_O 79)
(defconst KEY_P 80)
(defconst KEY_Q 81)
(defconst KEY_R 82)
(defconst KEY_S 83)
(defconst KEY_T 84)
(defconst KEY_U 85)
(defconst KEY_V 86)
(defconst KEY_W 87)
(defconst KEY_X 88)
(defconst KEY_Y 89)
(defconst KEY_Z 90)
(defconst KEY_LEFT_BRACKET 91)
(defconst KEY_BACKSLASH 92)
(defconst KEY_RIGHT_BRACKET 93)
(defconst KEY_GRAVE 96)
;    // Function keys
(defconst KEY_SPACE 32)
(defconst KEY_ESCAPE 256)
(defconst KEY_ENTER 257)
(defconst KEY_TAB 258)
(defconst KEY_BACKSPACE 259)
(defconst KEY_INSERT 260)
(defconst KEY_DELETE 261)
(defconst KEY_RIGHT 262)
(defconst KEY_LEFT 263)
(defconst KEY_DOWN 264)
(defconst KEY_UP 265)
(defconst KEY_PAGE_UP 266)
(defconst KEY_PAGE_DOWN 267)
(defconst KEY_HOME 268)
(defconst KEY_END 269)
(defconst KEY_CAPS_LOCK 280)
(defconst KEY_SCROLL_LOCK 281)
(defconst KEY_NUM_LOCK 282)
(defconst KEY_PRINT_SCREEN 283)
(defconst KEY_PAUSE 284)
(defconst KEY_F1 290)
(defconst KEY_F2 291)
(defconst KEY_F3 292)
(defconst KEY_F4 293)
(defconst KEY_F5 294)
(defconst KEY_F6 295)
(defconst KEY_F7 296)
(defconst KEY_F8 297)
(defconst KEY_F9 298)
(defconst KEY_F10 299)
(defconst KEY_F11 300)
(defconst KEY_F12 301)
(defconst KEY_LEFT_SHIFT 340)
(defconst KEY_LEFT_CONTROL 341)
(defconst KEY_LEFT_ALT 342)
(defconst KEY_LEFT_SUPER 343)
(defconst KEY_RIGHT_SHIFT 344)
(defconst KEY_RIGHT_CONTROL 345)
(defconst KEY_RIGHT_ALT 346)
(defconst KEY_RIGHT_SUPER 347)
(defconst KEY_KB_MENU 348)
;    // Keypad keys
(defconst KEY_KP_0 320)
(defconst KEY_KP_1 321)
(defconst KEY_KP_2 322)
(defconst KEY_KP_3 323)
(defconst KEY_KP_4 324)
(defconst KEY_KP_5 325)
(defconst KEY_KP_6 326)
(defconst KEY_KP_7 327)
(defconst KEY_KP_8 328)
(defconst KEY_KP_9 329)
(defconst KEY_KP_DECIMAL 330)
(defconst KEY_KP_DIVIDE 331)
(defconst KEY_KP_MULTIPLY 332)
(defconst KEY_KP_SUBTRACT 333)
(defconst KEY_KP_ADD 334)
(defconst KEY_KP_ENTER 335)
(defconst KEY_KP_EQUAL 336)
;    // Android key buttons
(defconst KEY_BACK 4)
(defconst KEY_MENU 82)
(defconst KEY_VOLUME_UP 24)
(defconst KEY_VOLUME_DOWN 2)
;} KeyboardKey;
;
;// Add backwards compatibility support for deprecated names
;#define MOUSE_LEFT_BUTTON   MOUSE_BUTTON_LEFT
;#define MOUSE_RIGHT_BUTTON  MOUSE_BUTTON_RIGHT
;#define MOUSE_MIDDLE_BUTTON MOUSE_BUTTON_MIDDLE
;
;// Mouse buttons
;typedef enum {
(defconst MOUSE_BUTTON_LEFT 0)
(defconst MOUSE_BUTTON_RIGHT 1)
(defconst MOUSE_BUTTON_MIDDLE 2)
(defconst MOUSE_BUTTON_SIDE 3)
(defconst MOUSE_BUTTON_EXTRA 4)
(defconst MOUSE_BUTTON_FORWARD 5)
(defconst MOUSE_BUTTON_BACK 6)
;} MouseButton;
;
;// Mouse cursor
;typedef enum {
(defconst MOUSE_CURSOR_DEFAULT 0)
(defconst MOUSE_CURSOR_ARROW 1)
(defconst MOUSE_CURSOR_IBEAM 2)
(defconst MOUSE_CURSOR_CROSSHAIR 3)
(defconst MOUSE_CURSOR_POINTING_HAND 4)
(defconst MOUSE_CURSOR_RESIZE_EW 5)
(defconst MOUSE_CURSOR_RESIZE_NS 6)
(defconst MOUSE_CURSOR_RESIZE_NWSE 7)
(defconst MOUSE_CURSOR_RESIZE_NESW 8)
(defconst MOUSE_CURSOR_RESIZE_ALL 9)
(defconst MOUSE_CURSOR_NOT_ALLOWED 1)
;} MouseCursor;
;
;// Gamepad buttons
;typedef enum {
(defconst GAMEPAD_BUTTON_UNKNOWN 0)
(defconst GAMEPAD_BUTTON_LEFT_FACE_UP 1)
(defconst GAMEPAD_BUTTON_LEFT_FACE_RIGHT 2)
(defconst GAMEPAD_BUTTON_LEFT_FACE_DOWN 3)
(defconst GAMEPAD_BUTTON_LEFT_FACE_LEFT 4)
(defconst GAMEPAD_BUTTON_RIGHT_FACE_UP 5)
(defconst GAMEPAD_BUTTON_RIGHT_FACE_RIGHT 6)
(defconst GAMEPAD_BUTTON_RIGHT_FACE_DOWN 7)
(defconst GAMEPAD_BUTTON_RIGHT_FACE_LEFT 8)
(defconst GAMEPAD_BUTTON_LEFT_TRIGGER_1 9)
(defconst GAMEPAD_BUTTON_LEFT_TRIGGER_2 10)
(defconst GAMEPAD_BUTTON_RIGHT_TRIGGER_1 11)
(defconst GAMEPAD_BUTTON_RIGHT_TRIGGER_2 12)
(defconst GAMEPAD_BUTTON_MIDDLE_LEFT 13)
(defconst GAMEPAD_BUTTON_MIDDLE 14)
(defconst GAMEPAD_BUTTON_MIDDLE_RIGHT 15)
(defconst GAMEPAD_BUTTON_LEFT_THUMB 16)
(defconst GAMEPAD_BUTTON_RIGHT_THUMB 17)
;} GamepadButton;
;
;// Gamepad axis
;typedef enum {
(defconst GAMEPAD_AXIS_LEFT_X 0)
(defconst GAMEPAD_AXIS_LEFT_Y 1)
(defconst GAMEPAD_AXIS_RIGHT_X 2)
(defconst GAMEPAD_AXIS_RIGHT_Y 3)
(defconst GAMEPAD_AXIS_LEFT_TRIGGER 4)
(defconst GAMEPAD_AXIS_RIGHT_TRIGGER 5)
;} GamepadAxis;
;
;// :material map index
;typedef enum {
(defconst MATERIAL_MAP_ALBEDO 0)
(defconst MATERIAL_MAP_METALNESS 1)
(defconst MATERIAL_MAP_NORMAL 2)
(defconst MATERIAL_MAP_ROUGHNESS 3)
(defconst MATERIAL_MAP_OCCLUSION 4 )
(defconst MATERIAL_MAP_EMISSION 5)
(defconst MATERIAL_MAP_HEIGHT 6)
(defconst MATERIAL_MAP_CUBEMAP 7)
(defconst MATERIAL_MAP_IRRADIANCE 8)
(defconst MATERIAL_MAP_PREFILTER 9)
(defconst MATERIAL_MAP_BRDF 10)
;} MaterialMapIndex;
;
(defconst MATERIAL_MAP_DIFFUSE MATERIAL_MAP_ALBEDO)
(defconst MATERIAL_MAP_SPECULAR MATERIAL_MAP_METALNESS)
;
;// :shader location index
;typedef enum {
(defconst SHADER_LOC_VERTEX_POSITION 0)
(defconst SHADER_LOC_VERTEX_TEXCOORD01 1)
(defconst SHADER_LOC_VERTEX_TEXCOORD02 2)
(defconst SHADER_LOC_VERTEX_NORMAL 3)
(defconst SHADER_LOC_VERTEX_TANGENT 4)
(defconst SHADER_LOC_VERTEX_COLOR 5)
(defconst SHADER_LOC_MATRIX_MVP 6)
(defconst SHADER_LOC_MATRIX_VIEW 7)
(defconst SHADER_LOC_MATRIX_PROJECTION 8)
(defconst SHADER_LOC_MATRIX_MODEL 9)
(defconst SHADER_LOC_MATRIX_NORMAL 10)
(defconst SHADER_LOC_VECTOR_VIEW 11)
(defconst SHADER_LOC_COLOR_DIFFUSE 12)
(defconst SHADER_LOC_COLOR_SPECULAR 13)
(defconst SHADER_LOC_COLOR_AMBIENT 14)
(defconst SHADER_LOC_MAP_ALBEDO 15)
(defconst SHADER_LOC_MAP_METALNESS 16)
(defconst SHADER_LOC_MAP_NORMAL 17)
(defconst SHADER_LOC_MAP_ROUGHNESS 18)
(defconst SHADER_LOC_MAP_OCCLUSION 19)
(defconst SHADER_LOC_MAP_EMISSION 20)
(defconst SHADER_LOC_MAP_HEIGHT 21)
(defconst SHADER_LOC_MAP_CUBEMAP 22)
(defconst SHADER_LOC_MAP_IRRADIANCE 23)
(defconst SHADER_LOC_MAP_PREFILTER 24)
(defconst SHADER_LOC_MAP_BRDF 25)
;ShaderLocationIndex;
;
(defconst SHADER_LOC_MAP_DIFFUSE SHADER_LOC_MAP_ALBEDO)
(defconst SHADER_LOC_MAP_SPECULAR SHADER_LOC_MAP_METALNESS)
;
;// :shader uniform data type
;typedef enum {
(defconst SHADER_UNIFORM_FLOAT 0)
(defconst SHADER_UNIFORM_VEC2 1)
(defconst SHADER_UNIFORM_VEC3 2)
(defconst SHADER_UNIFORM_VEC4 3)
(defconst SHADER_UNIFORM_INT 4)
(defconst SHADER_UNIFORM_IVEC2 5)
(defconst SHADER_UNIFORM_IVEC3 6)
(defconst SHADER_UNIFORM_IVEC4 7)
(defconst SHADER_UNIFORM_SAMPLER2D 8)
;} ShaderUniformDataType;
;
;// :shader attribute data types
;typedef enum {
(defconst SHADER_ATTRIB_FLOAT 0)
(defconst SHADER_ATTRIB_VEC2 1)
(defconst SHADER_ATTRIB_VEC3 2)
(defconst SHADER_ATTRIB_VEC4 3)
;} ShaderAttributeDataType;
;
;// Pixel formats
;// NOTE: Support depends on OpenGL version and platform
;typedef enum {
(defconst PIXELFORMAT_UNCOMPRESSED_GRAYSCALE 1)
(defconst PIXELFORMAT_UNCOMPRESSED_GRAY_ALPHA 2)
(defconst PIXELFORMAT_UNCOMPRESSED_R5G6B5 3)
(defconst PIXELFORMAT_UNCOMPRESSED_R8G8B8 4)
(defconst PIXELFORMAT_UNCOMPRESSED_R5G5B5A1 5)
(defconst PIXELFORMAT_UNCOMPRESSED_R4G4B4A4 6)
(defconst PIXELFORMAT_UNCOMPRESSED_R8G8B8A8 7)
(defconst PIXELFORMAT_UNCOMPRESSED_R32 8)
(defconst PIXELFORMAT_UNCOMPRESSED_R32G32B32 9)
(defconst PIXELFORMAT_UNCOMPRESSED_R32G32B32A32 10)
(defconst PIXELFORMAT_UNCOMPRESSED_R16 11)
(defconst PIXELFORMAT_UNCOMPRESSED_R16G16B16 12)
(defconst PIXELFORMAT_UNCOMPRESSED_R16G16B16A16 13)
(defconst PIXELFORMAT_COMPRESSED_DXT1_RGB 14)
(defconst PIXELFORMAT_COMPRESSED_DXT1_RGBA 15)
(defconst PIXELFORMAT_COMPRESSED_DXT3_RGBA 16)
(defconst PIXELFORMAT_COMPRESSED_DXT5_RGBA 17)
(defconst PIXELFORMAT_COMPRESSED_ETC1_RGB 18)
(defconst PIXELFORMAT_COMPRESSED_ETC2_RGB 19)
(defconst PIXELFORMAT_COMPRESSED_ETC2_EAC_RGBA 20)
(defconst PIXELFORMAT_COMPRESSED_PVRT_RGB 21)
(defconst PIXELFORMAT_COMPRESSED_PVRT_RGBA 22)
(defconst PIXELFORMAT_COMPRESSED_ASTC_4x4_RGBA 23)
(defconst PIXELFORMAT_COMPRESSED_ASTC_8x8_RGB 24)
;} PixelFormat;
;
;// :texture parameters: filter mode
;// NOTE 1: Filtering considers mipmaps if available in the texture
;// NOTE 2: Filter is accordingly set for minification and magnification
;typedef enum {
(defconst TEXTURE_FILTER_POINT 0)
(defconst TEXTURE_FILTER_BILINEAR 1)
(defconst TEXTURE_FILTER_TRILINEAR 2)
(defconst TEXTURE_FILTER_ANISOTROPIC_4X 3)
(defconst TEXTURE_FILTER_ANISOTROPIC_8X 4)
(defconst TEXTURE_FILTER_ANISOTROPIC_16X 5)
;} TextureFilter;
;
;// :texture parameters: wrap mode
;typedef enum {
(defconst TEXTURE_WRAP_REPEAT 0)
(defconst TEXTURE_WRAP_CLAMP 1)
(defconst TEXTURE_WRAP_MIRROR_REPEAT 2)
(defconst TEXTURE_WRAP_MIRROR_CLAMP 3)
;} TextureWrap;
;
;// Cubemap layouts
;typedef enum {
(defconst CUBEMAP_LAYOUT_AUTO_DETECT 0)
(defconst CUBEMAP_LAYOUT_LINE_VERTICAL 1)
(defconst CUBEMAP_LAYOUT_LINE_HORIZONTAL 2)
(defconst CUBEMAP_LAYOUT_CROSS_THREE_BY_FOUR 3)
(defconst CUBEMAP_LAYOUT_CROSS_FOUR_BY_THREE 4)
(defconst CUBEMAP_LAYOUT_PANORAMA 5)
;} CubemapLayout;
;
;// :font type, defines generation method
;typedef enum {
(defconst FONT_DEFAULT 0)
(defconst FONT_BITMAP 1)
(defconst FONT_SDF 2)
;} FontType;
;
;// :color blending modes (pre-defined)
;typedef enum {
(defconst BLEND_ALPHA 0)
(defconst BLEND_ADDITIVE 1)
(defconst BLEND_MULTIPLIED 2)
(defconst BLEND_ADD_COLORS 3)
(defconst BLEND_SUBTRACT_COLORS 4)
(defconst BLEND_ALPHA_PREMULTIPLY 5)
(defconst BLEND_CUSTOM 6)
(defconst BLEND_CUSTOM_SEPARATE 7)
;} BlendMode;
;
;// Gesture
;// NOTE: Provided as bit-wise flags to enable only desired gestures
;typedef enum {
(defconst GESTURE_NONE 0)
(defconst GESTURE_TAP 1)
(defconst GESTURE_DOUBLETAP 2)
(defconst GESTURE_HOLD 4)
(defconst GESTURE_DRAG 8)
(defconst GESTURE_SWIPE_RIGHT 16)
(defconst GESTURE_SWIPE_LEFT 32)
(defconst GESTURE_SWIPE_UP 64)
(defconst GESTURE_SWIPE_DOWN 128)
(defconst GESTURE_PINCH_IN 256)
(defconst GESTURE_PINCH_OUT 51)
;} Gesture;
;
;// :camera-3d system modes
;typedef enum {
(defconst CAMERA_CUSTOM 0)
(defconst CAMERA_FREE 1)
(defconst CAMERA_ORBITAL 2)
(defconst CAMERA_FIRST_PERSON 3)
(defconst CAMERA_THIRD_PERSON 4)
;} CameraMode;
;
;// :camera-3d projection
;typedef enum {
(defconst CAMERA_PERSPECTIVE 0)
(defconst CAMERA_ORTHOGRAPHIC 1)
;} CameraProjection;
;
;// N-patch layout
;typedef enum {
(defconst NPATCH_NINE_PATCH 0)
(defconst NPATCH_THREE_PATCH_VERTICAL 1)
(defconst NPATCH_THREE_PATCH_HORIZONTAL 2)
;} NPatchLayout;

(cffi/defcfn strlen
  "Given a string, measures its length in bytes."
  strlen [::mem/c-string] ::mem/long)

(cffi/load-library "raylib.dll")

(cffi/defcfn init-window
  "[width height title]"
  InitWindow [::mem/int ::mem/int ::mem/c-string] ::mem/void)

(comment
  (init-window 800 800 "henlo from clojure??")

  )

(def old-types->new-types
  {:int8   ::mem/byte
   :int16  ::mem/short
   :int32  ::mem/int
   :uint8  ::mem/byte
   :uint16 ::mem/short
   :uint32 ::mem/int
   :void   ::mem/void
   })

(defn type-converter [old]
  (let [new-typename (old-types->new-types old)]
    (cond
      new-typename new-typename
      (vector? old) (recur (second old))
      :else (keyword "raylib-clj.core" (name old))
     ))
  )

(defn arg-decl->arg-name [old]
  (if (vector? old) (str (name (first old)))))

(defn coffify [fn-name-old signature]
  (let [fn-name-new (symbol (csk/->kebab-case (name fn-name-old)))
        rettype (type-converter (:rettype signature))
        argtypes (vec (map type-converter (:argtypes signature)))
        argnames (filter identity (map arg-decl->arg-name (:argtypes signature)))
        argnames-str (clojure.string/join " " argnames)
        boolean-return? (and (clojure.string/starts-with? fn-name-new "is-") (= rettype ::mem/byte))
        ]
    `(cffi/defcfn ~(if boolean-return?
                     (str (clojure.string/replace-first (str fn-name-new) "is-" "") "?")
                     fn-name-new
                     )
       ~(str "[" argnames-str "]" " -> " (if boolean-return? "bool" (name rettype)))
       ~(symbol (name fn-name-old))
       ~argtypes
       ~rettype
       )
    )
  )

(coffi.ffi/defcfn
  window-should-close
  "[] -> bool"
  WindowShouldClose
  []
  :coffi.mem/byte)

(coffi.ffi/defcfn
  close-window
  "[] -> void"
  CloseWindow
  []
  :coffi.mem/void)

(coffi.ffi/defcfn
  window-ready?
  "[] -> bool"
  IsWindowReady
  []
  :coffi.mem/byte)

(coffi.ffi/defcfn
  window-fullscreen?
  "[ ] -> bool"
  IsWindowFullscreen
  []
  :coffi.mem/byte)

(coffi.ffi/defcfn
  window-hidden?
  "[ ] -> bool"
  IsWindowHidden
  []
  :coffi.mem/byte)

(coffi.ffi/defcfn
  window-minimized?
  "[ ] -> bool"
  IsWindowMinimized
  []
  :coffi.mem/byte)

(coffi.ffi/defcfn
  window-maximized?
  "[ ] -> bool"
  IsWindowMaximized
  []
  :coffi.mem/byte)

(coffi.ffi/defcfn
  window-focused?
  "[] -> bool"
  IsWindowFocused
  []
  :coffi.mem/byte)

(coffi.ffi/defcfn
  window-resized?
  "[] -> bool"
  IsWindowResized
  []
  :coffi.mem/byte)

(coffi.ffi/defcfn
  window-state?
  "[flag] -> bool"
  IsWindowState
  [:coffi.mem/int]
  :coffi.mem/byte)

(coffi.ffi/defcfn
  set-window-state
  "[flags] -> void"
  SetWindowState
  [:coffi.mem/int]
  :coffi.mem/void)

(coffi.ffi/defcfn
  clear-window-state
  "[flags] -> void"
  ClearWindowState
  [:coffi.mem/int]
  :coffi.mem/void)

(coffi.ffi/defcfn
  toggle-fullscreen
  "[] -> void"
  ToggleFullscreen
  []
  :coffi.mem/void)

(comment

  ;TODO:this thing doesn't work... whats the actual symbol in the shared library?
  ;it seems like the function simply isn't in the exports of the library...
  ;is this a build issue? is there some sort of flag that must be enabled before
  ;the shared library contains the function?
  ;the shared library *should* have been built with PLATFORM=DESKTOP...
  (coffi.ffi/defcfn
   toggle-borderless-windowed
   "[] -> void"
   ToggleBorderlessWindowed
   []
   :coffi.mem/void)
  )

(coffi.ffi/defcfn
  maximize-window
  "[] -> void"
  MaximizeWindow
  []
  :coffi.mem/void)

(coffi.ffi/defcfn
  minimize-window
  "[] -> void"
  MinimizeWindow
  []
  :coffi.mem/void)
(coffi.ffi/defcfn
  restore-window
  "[] -> void"
  RestoreWindow
  []
  :coffi.mem/void)
(comment
  ;TODO: doesn't load
  (coffi.ffi/defcfn
    set-window-opacity
    "[opacity] -> void"
    SetWindowOpacity
    [:raylib-clj.core/float32]
    :coffi.mem/void)
  (coffi.ffi/defcfn
    set-window-icons
    "[images count] -> void"
    SetWindowIcons
    [:coffi.mem/pointer :coffi.mem/int]
    :coffi.mem/void))
(coffi.ffi/defcfn
  set-window-focused
  "[] -> void"
  SetWindowFocused
  []
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-window-min-size
  "[width height] -> void"
  SetWindowMinSize
  [:coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-window-size
  "[width height] -> void"
  SetWindowSize
  [:coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(comment ;TODO
  (coffi.ffi/defcfn
   set-window-title
   "[title] -> void"
   SetWindowTitle
   [:raylib-clj.core/ptr]
   :coffi.mem/void))
(coffi.ffi/defcfn
  set-window-position
  "[x y] -> void"
  SetWindowPosition
  [:coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-window-monitor
  "[monitor] -> void"
  SetWindowMonitor
  [:coffi.mem/int]
  :coffi.mem/void)
(comment;TODO
  (coffi.ffi/defcfn
   get-window-handle
   "[] -> pointer"
   GetWindowHandle
   []
   :coffi.mem/pointer))
(coffi.ffi/defcfn
  set-window-icon
  "[image] -> void"
  SetWindowIcon
  [:raylib-clj.core/image]
  :coffi.mem/void)

(comment;TODO
  (coffi.ffi/defcfn
   get-clipboard-text
   "[] -> pointer"
   GetClipboardText
   []
   :coffi.mem/pointer))
(coffi.ffi/defcfn
  disable-event-waiting
  "[] -> void"
  DisableEventWaiting
  []
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-render-width
  "[] -> int"
  GetRenderWidth
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-screen-width
  "[] -> int"
  GetScreenWidth
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-monitor-width
  "[monitor] -> int"
  GetMonitorWidth
  [:coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-screen-height
  "[] -> int"
  GetScreenHeight
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-monitor-refresh-rate
  "[monitor] -> int"
  GetMonitorRefreshRate
  [:coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-window-position
  "[] -> vec2"
  GetWindowPosition
  []
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  get-monitor-height
  "[monitor] -> int"
  GetMonitorHeight
  [:coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-window-scale-dpi
  "[] -> vec2"
  GetWindowScaleDPI
  []
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  get-current-monitor
  "[] -> int"
  GetCurrentMonitor
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-render-height
  "[] -> int"
  GetRenderHeight
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-monitor-physical-height
  "[monitor] -> int"
  GetMonitorPhysicalHeight
  [:coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-monitor-count
  "[] -> int"
  GetMonitorCount
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-monitor-position
  "[monitor] -> vec2"
  GetMonitorPosition
  [:coffi.mem/int]
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  enable-event-waiting
  "[] -> void"
  EnableEventWaiting
  []
  :coffi.mem/void)
(comment;TODO
  (coffi.ffi/defcfn
   get-monitor-physical-width
   "[monitor] -> nt32"
   GetMonitorPhysicalWidth
   [:coffi.mem/int]
   :raylib-clj.core/nt32))
(comment;TODO
  (coffi.ffi/defcfn
   get-monitor-name
   "[monitor] -> pointer"
   GetMonitorName
   [:coffi.mem/int]
   :coffi.mem/pointer))
(comment;TODO
  (coffi.ffi/defcfn
   set-clipboard-text
   "[text] -> void"
   SetClipboardText
   [:coffi.mem/pointer]
   :coffi.mem/void))

(comment;TODO
  (coffi.ffi/defcfn
   get-clipboard-text
   "[] -> pointer"
   GetClipboardText
   []
   :coffi.mem/pointer))
(coffi.ffi/defcfn
  enable-event-waiting
  "[] -> void"
  EnableEventWaiting
  []
  :coffi.mem/void)
(coffi.ffi/defcfn
  disable-event-waiting
  "[] -> void"
  DisableEventWaiting
  []
  :coffi.mem/void)

(coffi.ffi/defcfn
  swap-screen-buffer
  "[] -> void"
  SwapScreenBuffer
  []
  :coffi.mem/void)
(coffi.ffi/defcfn end-mode-3-d "[] -> void" EndMode3D [] :coffi.mem/void)
(comment;TODO
  (coffi.ffi/defcfn
   wait-time
   "[seconds] -> void"
   WaitTime
   [:raylib-clj.core/float64]
   :coffi.mem/void))
(coffi.ffi/defcfn
  begin-mode-2-d
  "[camera] -> void"
  BeginMode2D
  [:raylib-clj.core/camera-2d]
  :coffi.mem/void)
(comment;TODO
  (coffi.ffi/defcfn
   "cursor-on-screen?"
   "[] -> bool"
   IsCursorOnScreen
   []
   :coffi.mem/byte))
(coffi.ffi/defcfn end-mode-2-d "[] -> void" EndMode2D [] :coffi.mem/void)
(coffi.ffi/defcfn
  clear-background
  "[color] -> void"
  ClearBackground
  [:raylib-clj.core/color]
  :coffi.mem/void)
(comment;TODO
  (coffi.ffi/defcfn
   "cursor-hidden?"
   "[] -> bool"
   IsCursorHidden
   []
   :coffi.mem/byte))
(coffi.ffi/defcfn
  begin-mode-3-d
  "[camera] -> void"
  BeginMode3D
  [:raylib-clj.core/camera-3d]
  :coffi.mem/void)
(coffi.ffi/defcfn
  begin-texture-mode
  "[target] -> void"
  BeginTextureMode
  [:raylib-clj.core/render-texture]
  :coffi.mem/void)
(coffi.ffi/defcfn end-blend-mode "[] -> void" EndBlendMode [] :coffi.mem/void)
(coffi.ffi/defcfn
  end-texture-mode
  "[] -> void"
  EndTextureMode
  []
  :coffi.mem/void)
(coffi.ffi/defcfn begin-drawing "[] -> void" BeginDrawing [] :coffi.mem/void)
(coffi.ffi/defcfn show-cursor "[] -> void" ShowCursor [] :coffi.mem/void)
(coffi.ffi/defcfn hide-cursor "[] -> void" HideCursor [] :coffi.mem/void)
(coffi.ffi/defcfn
  end-shader-mode
  "[] -> void"
  EndShaderMode
  []
  :coffi.mem/void)
(coffi.ffi/defcfn
  begin-shader-mode
  "[shader] -> void"
  BeginShaderMode
  [:raylib-clj.core/shader]
  :coffi.mem/void)
(coffi.ffi/defcfn end-drawing "[] -> void" EndDrawing [] :coffi.mem/void)
(coffi.ffi/defcfn
  poll-input-events
  "[] -> void"
  PollInputEvents
  []
  :coffi.mem/void)
(coffi.ffi/defcfn enable-cursor "[] -> void" EnableCursor [] :coffi.mem/void)
(coffi.ffi/defcfn
  disable-cursor
  "[] -> void"
  DisableCursor
  []
  :coffi.mem/void)
(coffi.ffi/defcfn
  begin-blend-mode
  "[mode] -> void"
  BeginBlendMode
  [:coffi.mem/int]
  :coffi.mem/void)

;font stuff 
(comment;TODO
  (coffi.ffi/defcfn
   "font-ready?"
   "[font] -> bool"
   IsFontReady
   [:raylib-clj.core/font]
   :coffi.mem/byte))
(coffi.ffi/defcfn
  export-font-as-code
  "[font fileName] -> byte"
  ExportFontAsCode
  [:raylib-clj.core/font :coffi.mem/pointer]
  :coffi.mem/byte)
(comment;TODO
  (coffi.ffi/defcfn
   load-font-data
   "[fileData dataSize fontSize fontChars glyphCount type] -> point"
   LoadFontData
   [:coffi.mem/pointer
    :coffi.mem/int
    :coffi.mem/int
    :coffi.mem/pointer
    :coffi.mem/int
    :coffi.mem/int]
   :raylib-clj.core/point))
(coffi.ffi/defcfn
  get-font-default
  "[] -> font"
  GetFontDefault
  []
  :raylib-clj.core/font)
(coffi.ffi/defcfn
  unload-font-data
  "[chars glyphCount] -> void"
  UnloadFontData
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-font
  "[fileName] -> font"
  LoadFont
  [:coffi.mem/pointer]
  :raylib-clj.core/font)
(comment;TODO
  (coffi.ffi/defcfn
   draw-text-pro
   "[font text position origin rotation fontSize spacing tint] -> void"
   DrawTextPro
   [:raylib-clj.core/font
    :coffi.mem/pointer
    :raylib-clj.core/vec2
    :raylib-clj.core/vec2
    :raylib-clj.core/float32
    :raylib-clj.core/float32
    :raylib-clj.core/float32
    :raylib-clj.core/color]
   :coffi.mem/void))
(coffi.ffi/defcfn
  draw-fps
  "[posX posY] -> void"
  DrawFPS
  [:coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-font-from-image
  "[image key firstChar] -> font"
  LoadFontFromImage
  [:raylib-clj.core/image :raylib-clj.core/color :coffi.mem/int]
  :raylib-clj.core/font)
(coffi.ffi/defcfn
  load-font-ex
  "[fileName fontSize fontChars glyphCount] -> font"
  LoadFontEx
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int]
  :raylib-clj.core/font)
(coffi.ffi/defcfn
  unload-font
  "[font] -> void"
  UnloadFont
  [:raylib-clj.core/font]
  :coffi.mem/void)
(comment;TODO
  (coffi.ffi/defcfn
   draw-text-codepoints
   "[font codepoints count position fontSize spacing tint] -> void"
   DrawTextCodepoints
   [:raylib-clj.core/font
    :coffi.mem/pointer
    :coffi.mem/int
    :raylib-clj.core/vec2
    :raylib-clj.core/float32
    :raylib-clj.core/float32
    :raylib-clj.core/color]
   :coffi.mem/void))
(comment;TODO
  (coffi.ffi/defcfn
   draw-text-ex
   "[font text position fontSize spacing tint] -> void"
   DrawTextEx
   [:raylib-clj.core/font
    :coffi.mem/pointer
    :raylib-clj.core/vec2
    :raylib-clj.core/float32
    :raylib-clj.core/float32
    :raylib-clj.core/color]
   :coffi.mem/void))
(comment;TODO
  (coffi.ffi/defcfn
   draw-text-codepoint
   "[font codepoint position fontSize tint] -> void"
   DrawTextCodepoint
   [:raylib-clj.core/font
    :coffi.mem/int
    :raylib-clj.core/vec2
    :raylib-clj.core/float32
    :raylib-clj.core/color]
   :coffi.mem/void))
(coffi.ffi/defcfn
  gen-image-font-atlas
  "[chars recs glyphCount fontSize padding packMethod] -> image"
  GenImageFontAtlas
  [:coffi.mem/pointer
   :coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  draw-text
  "[text posX posY fontSize color] -> void"
  DrawText
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-font-from-memory
  "[fileType fileData dataSize fontSize fontChars glyphCount] -> font"
  LoadFontFromMemory
  [:coffi.mem/pointer
   :coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int]
  :raylib-clj.core/font)
(comment

  (coffify
   :IsWindowState
   {:rettype :int8 :argtypes [['flag :int32]]}
   )

  (->> '{
         ;TODO: put old defs here
  }
       (map identity)
       (map #(coffify (first %) (second %)))
       )

  (defn byte->bool [b] (not= b 0))


  (do
    (init-window 800 450 "raylib-clj [core] example - basic window")
    (while (not (byte->bool (window-should-close)))
      (begin-drawing)
      (clear-background RAYWHITE)
      (draw-text "Congrats! You created your first window!" 190 200 20 BLACK);
      (end-drawing)
      )
    (close-window)
    )


)

(def LIGHTGRAY  [ 200, 200, 200, 255    ])
(def GRAY       [ 130, 130, 130, 255    ])
(def DARKGRAY   [ 80, 80, 80, 255       ])
(def YELLOW     [ 253, 249, 0, 255      ])
(def GOLD       [ 255, 203, 0, 255      ])
(def ORANGE     [ 255, 161, 0, 255      ])
(def PINK       [ 255, 109, 194, 255    ])
(def RED        [ 230, 41, 55, 255      ])
(def MAROON     [ 190, 33, 55, 255      ])
(def GREEN      [ 0, 228, 48, 255       ])
(def LIME       [ 0, 158, 47, 255       ])
(def DARKGREEN  [ 0, 117, 44, 255       ])
(def SKYBLUE    [ 102, 191, 255, 255    ])
(def BLUE       [ 0, 121, 241, 255      ])
(def DARKBLUE   [ 0, 82, 172, 255       ])
(def PURPLE     [ 200, 122, 255, 255    ])
(def VIOLET     [ 135, 60, 190, 255     ])
(def DARKPURPLE [ 112, 31, 126, 255     ])
(def BEIGE      [ 211, 176, 131, 255    ])
(def BROWN      [ 127, 106, 79, 255     ])
(def DARKBROWN  [ 76, 63, 47, 255       ])

(def WHITE      [ 255, 255, 255, 255    ])
(def BLACK      [ 0, 0, 0, 255          ])
(def BLANK      [ 0, 0, 0, 0            ])
(def MAGENTA    [ 255, 0, 255, 255      ])
(def RAYWHITE   [ 245, 245, 245, 255    ])



(comment (ffi/define-library! native-part-1

'{

                                        ; ;frame control functions
     ;;// NOTE: Those functions are intended for advance users that want full control over the frame processing
     ;;// By default EndDrawing() does this job: draws everything + SwapScreenBuffer() + manage frame timing + PollInputEvents()
     ;;// To avoid that behaviour and control frame processes manually, enable in config.h: SUPPORT_CUSTOM_FRAME_CONTROL
     :BeginScissorMode {:rettype :void :argtypes [[x :int32] [y :int32] [width :int32] [height :int32]]}
     :EndScissorMode {:rettype :void :argtypes []}
     :BeginVrStereoMode {:rettype :void :argtypes [[config :vr-stereo-config]]}
     :EndVrStereoMode {:rettype :void :argtypes []}
     ;; stereo config functions for VR simulator
     :LoadVrStereoConfig  {:rettype :vr-stereo-config :argtypes [[device :vr-device-info]]}
     :UnloadVrStereoConfig {:rettype :void :argtypes [[config :vr-stereo-config]]}
     ;; management functions
     ;;// NOTE: :shader functionality is not available on OpenGL 1.1
     :LoadShader {:rettype :shader :argtypes [[vsFileName :pointer] [fsFileName :pointer]]}
     :LoadShaderFromMemory {:rettype :shader :argtypes [[vsCode :pointer] [fsCode :pointer]]}
     :IsShaderReady {:rettype :int8 :argtypes [[shader :shader]]}
     :GetShaderLocation {:rettype :int32 :argtypes [[shader :shader] [uniformName :pointer]]}
     :GetShaderLocationAttrib {:rettype :int32 :argtypes [[shader :shader] [attribName :pointer]]}
     :SetShaderValue {:rettype :void :argtypes [[shader :shader] [locIndex :int32]  [value :pointer] [uniformType :int32]]}
     :SetShaderValueV {:rettype :void :argtypes [[shader :shader] [locIndex :int32]  [value :pointer] [uniformType :int32] [count :int32]]}
     :SetShaderValueMatrix {:rettype :mat4 :argtypes [[shader :shader] [locIndex :int32] [mat :mat4]]}
     :SetShaderValueTexture {:rettype :texture :argtypes [[shader :shader] [locIndex :int32] [texture :texture]]}
     :UnloadShader {:rettype :shader :argtypes [[shader :shader]]}
     ;;-space-related functions

     :GetMouseRay {:rettype :ray :argtypes [[mousePosition :vec2] [camera :camera-3d]]}
     :GetCameraMatrix {:rettype :mat4 :argtypes [[camera :camera-3d]]}
     :GetCameraMatrix2D {:rettype :mat4 :argtypes [[camera :camera-2d]]}
     :GetWorldToScreen {:rettype :vec2 :argtypes [[position :vec3] [camera :camera-3d]]}
     :GetScreenToWorld2D {:rettype :vec2 :argtypes [[position :vec2] [camera :camera-2d]]}
     :GetWorldToScreenEx {:rettype :vec2 :argtypes [[position :vec3] [camera :camera-3d] [width :int32] [height :int32]]}
     :GetWorldToScreen2D {:rettype :vec2 :argtypes [[position :vec2] [camera :camera-2d]]}
     ;;-related functions

     :SetTargetFPS {:rettype :void :argtypes [[fps :int32]]}
     :GetFPS {:rettype :int32 :argtypes []}
     :GetFrameTime {:rettype :float :argtypes []}
     :GetTime {:rettype :double :argtypes []}
     ;;. functions

     :GetRandomValue {:rettype :int32 :argtypes [[min :int32] [max :int32]]}
     :SetRandomSeed {:rettype :void :argtypes [[seed :int32]]}
     :TakeScreenshot {:rettype :void :argtypes [[fileName :pointer]]}
     :SetConfigFlags {:rettype :void :argtypes [[flags :int32]]}
                                        ;TODO: what to do here???
                                        ;:void TraceLog {:rettype :LAPI :argtypes [:int32 logLevel, :pointer text, ...]} 
     :SetTraceLogLevel {:rettype :void :argtypes [[logLevel :int32]]}
     :MemAlloc {:rettype :pointer :argtypes [[size :int32]]}
     :MemRealloc {:rettype :pointer :argtypes [[ptr :pointer] [size :int32]]}
     :MemFree {:rettype :void :argtypes [[ptr :pointer]]}
     :OpenURL {:rettype :void :argtypes [[url :pointer]]}
     ;; custom callbacks
     ;;// WARNING: Callbacks setup is intended for advance users
                                        ;TODO: what are those types?
                                        ;    :SetTraceLogCallback {:rettype :void :argtypes [TraceLogCallback callback]}
                                        ;    :SetLoadFileDataCallback {:rettype :void :argtypes [LoadFileDataCallback callback]}
                                        ;    :SetSaveFileDataCallback {:rettype :void :argtypes [SaveFileDataCallback callback]}
                                        ;    :SetLoadFileTextCallback {:rettype :void :argtypes [LoadFileTextCallback callback]}
                                        ;    :SetSaveFileTextCallback {:rettype :void :argtypes [SaveFileTextCallback callback]}
     ;; management functions
     :LoadFileData {:rettype :pointer :argtypes [[fileName :pointer] [bytesRead :pointer]]}
     :UnloadFileData {:rettype :void :argtypes [[data :pointer]]}
     :SaveFileData {:rettype :int8 :argtypes [[fileName :pointer] [data :pointer] [bytesToWrite :int32]]}
     :ExportDataAsCode {:rettype :int8 :argtypes [[data :pointer] [size :int32] [fileName :pointer]]}
     :LoadFileText {:rettype :pointer :argtypes [[fileName :pointer]]}
     :UnloadFileText {:rettype :void :argtypes [[text :pointer]]}
     :SaveFileText {:rettype :int8 :argtypes [[fileName :pointer] [text :pointer]]}
     :FileExists {:rettype :int8 :argtypes [[fileName :pointer]]}
     :DirectoryExists {:rettype :int8 :argtypes [[dirPath :pointer]]}
     :IsFileExtension {:rettype :int8 :argtypes [[fileName :pointer] [ext :pointer]]}
     :GetFileLength {:rettype :int32 :argtypes [[fileName :pointer]]}
     :GetFileExtension {:rettype :pointer :argtypes [[fileName :pointer]]}
     :GetFileName {:rettype :pointer :argtypes [[filePath :pointer]]}
     :GetFileNameWithoutExt {:rettype :pointer :argtypes [[filePath :pointer]]}
     :GetDirectoryPath {:rettype :pointer :argtypes [[filePath :pointer]]}
     :GetPrevDirectoryPath {:rettype :pointer :argtypes [[dirPath :pointer]]}
     :GetWorkingDirectory {:rettype :pointer :argtypes []}
     :GetApplicationDirectory {:rettype :pointer :argtypes []}
     :ChangeDirectory {:rettype :int8 :argtypes [[dir :pointer]]}
     :IsPathFile {:rettype :int8 :argtypes [[path :pointer]]}
     :LoadDirectoryFiles {:rettype :file-path-list :argtypes [[dirPath :pointer]]}
     :LoadDirectoryFilesEx {:rettype :file-path-list :argtypes [[basePath :pointer] [filter :pointer] [scanSubdirs :int8]]}
     :UnloadDirectoryFiles {:rettype :void :argtypes [[files :file-path-list]]}
     :IsFileDropped {:rettype :int8 :argtypes []}
     :PathList {:rettype :file-path-list :argtypes []}
     :UnloadDroppedFiles {:rettype :void :argtypes [[files :file-path-list]]}
     :GetFileModTime {:rettype :long :argtypes [[fileName :pointer]]}
     ;;/Encoding functionality
     :CompressData {:rettype :pointer :argtypes [[data :pointer]  [dataSize :int32]  [compDataSize :pointer]]}
     :DecompressData {:rettype :pointer :argtypes [[compData :pointer]  [compDataSize :int32]  [dataSize :pointer]]}
     :EncodeDataBase64 {:rettype :pointer :argtypes [[data :pointer]  [dataSize :int32]  [outputSize :pointer]]}
     :DecodeDataBase64 {:rettype :pointer :argtypes [[data :pointer]  [outputSize :pointer]]}
     ;;   Input Handling Functions  {:rettype :;// :rettype }[Module: core]
     ;;//------------------------------------------------------------------------------------
     ;;
     ;;// Input-related functions: keyboard
     :IsKeyPressed {:rettype :int8 :argtypes [[key :int32]]}
     :IsKeyDown {:rettype :int8 :argtypes [[key :int32]]}
     :IsKeyReleased {:rettype :int8 :argtypes [[key :int32]]}
     :IsKeyUp {:rettype :int8 :argtypes [[key :int32]]}
     :SetExitKey {:rettype :void :argtypes [[key :int32]]}
     :GetKeyPressed {:rettype :int32 :argtypes []}
     :GetCharPressed {:rettype :int32 :argtypes []}
     ;;-related functions: gamepads
     :IsGamepadAvailable {:rettype :int8 :argtypes [[gamepad :int32]]}
     :GetGamepadName {:rettype :pointer :argtypes [[gamepad :int32]]}
     :IsGamepadButtonPressed {:rettype :int8 :argtypes [[gamepad :int32] [button :int32]]}
     :IsGamepadButtonDown {:rettype :int8 :argtypes [[gamepad :int32] [button :int32]]}
     :IsGamepadButtonReleased {:rettype :int8 :argtypes [[gamepad :int32] [button :int32]]}
     :IsGamepadButtonUp {:rettype :int8 :argtypes [[gamepad :int32] [button :int32]]}
     :GetGamepadButtonPressed {:rettype :int32 :argtypes []}
     :GetGamepadAxisCount {:rettype :int32 :argtypes [[gamepad :int32]]}
     :GetGamepadAxisMovement {:rettype :float :argtypes [[gamepad :int32] [axis :int32]]}
     :SetGamepadMappings {:rettype :int32 :argtypes [[mappings :pointer]]}
     ;;
     ;;// Input-related functions: mouse
     :IsMouseButtonPressed {:rettype :int8 :argtypes [[button :int32]]}
     :IsMouseButtonDown {:rettype :int8 :argtypes [[button :int32]]}
     :IsMouseButtonReleased {:rettype :int8 :argtypes [[button :int32]]}
     :IsMouseButtonUp {:rettype :int8 :argtypes [[button :int32]]}
     :GetMouseX {:rettype :int32 :argtypes []}
     :GetMouseY {:rettype :int32 :argtypes []}
     :GetMousePosition {:rettype :vec2 :argtypes []}
     :GetMouseDelta {:rettype :vec2 :argtypes []}
     :SetMousePosition {:rettype :void :argtypes [[x :int32] [y :int32]]}
     :SetMouseOffset {:rettype :void :argtypes [[offsetX :int32] [offsetY :int32]]}
     :SetMouseScale {:rettype :void :argtypes [[scaleX :float32] [scaleY :float32]]}
     :GetMouseWheelMove {:rettype :float :argtypes []}
     :GetMouseWheelMoveV {:rettype :vec2 :argtypes []}
     :SetMouseCursor {:rettype :void :argtypes [[cursor :int32]]}

     :GetTouchX {:rettype :int32 :argtypes []}
     :GetTouchY {:rettype :int32 :argtypes []}
     :GetTouchPosition {:rettype :vec2 :argtypes [[index :int32]]}
     :GetTouchPointId {:rettype :int32 :argtypes [[index :int32]]}
     :GetTouchPointCount {:rettype :int32 :argtypes []}
                                        ;Gestures and Touch Handling Functions  {:rettype :;// :rettype }[Module: rgestures]
     ;;//------------------------------------------------------------------------------------
     :SetGesturesEnabled {:rettype :void :argtypes [[flags :int32]]}
     :IsGestureDetected {:rettype :int8 :argtypes [[gesture :int32]]}
     :GetGestureDetected {:rettype :int32 :argtypes []}
     :GetGestureHoldDuration {:rettype :float :argtypes []}
     :GetGestureDragVector {:rettype :vec2 :argtypes []}
     :GetGestureDragAngle {:rettype :float :argtypes []}
     :GetGesturePinchVector {:rettype :vec2 :argtypes []}
     :GetGesturePinchAngle {:rettype :float :argtypes []}
                                        ;:camera-3d System Functions  {:rettype :;// :rettype }[Module: rcamera]
     ;;//------------------------------------------------------------------------------------
     :UpdateCamera3D {:rettype :void :argtypes [[camera-3d :pointer] [mode :int32]]}
     :UpdateCameraPro {:rettype :void :argtypes [[camera-3d :pointer] [movement :vec3] [rotation :vec3] [zoom :float32]]}
                                        ;Basic Shapes Drawing Functions  {:rettype :;// :rettype }[Module: shapes]
     ;;//------------------------------------------------------------------------------------
     ;;// Set texture and rectangle to be used on shapes drawing
     ;;// NOTE: It can be useful when using basic shapes and one single font,
     ;;// defining a font char white rectangle would allow drawing everything in a single draw call
     :SetShapesTexture {:rettype :void :argtypes [[texture :texture] [source  :rectangle]]}


     :DrawPixel {:rettype :void :argtypes [[posX :int32] [posY :int32] [color :color]]}
     :DrawPixelV {:rettype :void :argtypes [[position :vec2] [color  :color]]}
     :DrawLine {:rettype :void :argtypes [[startPosX :int32] [startPosY :int32] [endPosX :int32] [endPosY :int32] [color  :color]]}
     :DrawLineV {:rettype :void :argtypes [[startPos :vec2] [endPos :vec2] [color  :color]]}
     :DrawLineEx {:rettype :void :argtypes [[startPos :vec2] [endPos :vec2] [thick :float32] [color  :color]]}
     :DrawLineBezier {:rettype :void :argtypes [[startPos :vec2] [endPos :vec2] [thick :float32] [color  :color]]}
     :DrawLineBezierQuad {:rettype :void :argtypes [[startPos :vec2] [endPos :vec2] [controlPos :vec2] [thick :float32] [color  :color]]}
     :DrawLineBezierCubic {:rettype :void :argtypes [[startPos :vec2] [endPos :vec2] [startControlPos :vec2] [endControlPos :vec2] [thick :float32] [color  :color]]}
     :DrawLineBSpline {:rettype :void :argtypes [[points :pointer] [pointCount :int32] [thick :float32] [color  :color]]}
     :DrawLineCatmullRom {:rettype :void :argtypes [[points :pointer] [pointCount :int32] [thick :float32] [color  :color]]}
     :DrawLineStrip {:rettype :void :argtypes [[points :points] [pointCount :int32] [color  :color]]}
     :DrawCircle {:rettype :void :argtypes [[centerX :int32] [centerY :int32] [radius :float32] [color  :color]]}
     :DrawCircleSector {:rettype :void :argtypes [[center :vec2] [radius :float32] [startAngle :float32] [endAngle :float32] [segments :int32] [color  :color]]}
     :DrawCircleSectorLines {:rettype :void :argtypes [[center :vec2] [radius :float32] [startAngle :float32] [endAngle :float32] [segments :int32] [color  :color]]}
     :DrawCircleGradient {:rettype :void :argtypes [[centerX :int32] [centerY :int32] [radius :float32] [color1 :color] [color2  :color]]}
     :DrawCircleV {:rettype :void :argtypes [[center :vec2] [radius :float32] [color  :color]]}
     :DrawCircleLines {:rettype :void :argtypes [[centerX :int32] [centerY :int32] [radius :float32] [color  :color]]}
     :DrawEllipse {:rettype :void :argtypes [[centerX :int32] [centerY :int32] [radiusH :float32] [radiusV :float32] [color  :color]]}
     :DrawEllipseLines {:rettype :void :argtypes [[centerX :int32] [centerY :int32] [radiusH :float32] [radiusV :float32] [color  :color]]}
     :DrawRing {:rettype :void :argtypes [[center :vec2] [innerRadius :float32] [outerRadius :float32] [startAngle :float32] [endAngle :float32] [segments :int32] [color  :color]]}
     :DrawRingLines {:rettype :void :argtypes [[center :vec2] [innerRadius :float32] [outerRadius :float32] [startAngle :float32] [endAngle :float32] [segments :int32] [color  :color]]}
     :DrawRectangle {:rettype :void :argtypes [[posX :int32] [posY :int32] [width :int32] [height :int32] [color  :color]]}
     :DrawRectangleV {:rettype :void :argtypes [[position :vec2] [size :vec2] [color  :color]]}
     :DrawRectangleRec {:rettype :void :argtypes [[rec :rectangle] [color  :color]]}
     :DrawRectanglePro {:rettype :void :argtypes [[rec :rectangle] [origin :vec2] [rotation :float32] [color  :color]]}
     :DrawRectangleGradientV {:rettype :void :argtypes [[posX :int32] [posY :int32] [width :int32] [height :int32] [color1 :color] [color2  :color]]}
     :DrawRectangleGradientH {:rettype :void :argtypes [[posX :int32] [posY :int32] [width :int32] [height :int32] [color1 :color] [color2  :color]]}
     :DrawRectangleGradientEx {:rettype :void :argtypes [[rec :rectangle] [col1 :color] [col2 :color] [col3 :color] [col4  :color]]}
     :DrawRectangleLines {:rettype :void :argtypes [[posX :int32] [posY :int32] [width :int32] [height :int32] [color  :color]]}
     :DrawRectangleLinesEx {:rettype :void :argtypes [[rec :rectangle] [lineThick :float32] [color  :color]]}
     :DrawRectangleRounded {:rettype :void :argtypes [[rec :rectangle] [roundness :float32] [segments :int32] [color  :color]]}
     :DrawRectangleRoundedLines {:rettype :void :argtypes [[rec :rectangle] [roundness :float32] [segments :int32] [lineThick :float32] [color  :color]]}
     :DrawTriangle {:rettype :void :argtypes [[v1 :vec2] [v2 :vec2] [v3 :vec2] [color  :color]]}
     :DrawTriangleLines {:rettype :void :argtypes [[v1 :vec2] [v2 :vec2] [v3 :vec2] [color  :color]]}
     :DrawTriangleFan {:rettype :void :argtypes [[points :pointer] [pointCount :int32] [color  :color]]}
     :DrawTriangleStrip {:rettype :void :argtypes [[points :pointer] [pointCount :int32] [color  :color]]}
     :DrawPoly {:rettype :void :argtypes [[center :vec2] [sides :int32] [radius :float32] [rotation :float32] [color  :color]]}
     :DrawPolyLines {:rettype :void :argtypes [[center :vec2] [sides :int32] [radius :float32] [rotation :float32] [color  :color]]}
     :DrawPolyLinesEx {:rettype :void :argtypes [[center :vec2] [sides :int32] [radius :float32] [rotation :float32] [lineThick :float32] [color  :color]]}
     ;; shapes collision detection functions
     :CheckCollisionRecs {:rettype :int8 :argtypes [[rec1 :rectangle] [rec2 :rectangle]]}
     :CheckCollisionCircles {:rettype :int8 :argtypes [[center1 :vec2] [radius1 :float32] [center2 :vec2] [radius2 :float32]]}
     :CheckCollisionCircleRec {:rettype :int8 :argtypes [[center :vec2] [radius :float32] [rec :rectangle]]}
     :CheckCollisionPointRec {:rettype :int8 :argtypes [[point :vec2] [rec :rectangle]]}
     :CheckCollisionPointCircle {:rettype :int8 :argtypes [[point :vec2] [center :vec2] [radius :float32]]}
     :CheckCollisionPointTriangle {:rettype :int8 :argtypes [[point :vec2] [p1 :vec2] [p2 :vec2] [p3 :vec2]]}
     :CheckCollisionPointPoly {:rettype :int8 :argtypes [[point :vec2] [points :point] [pointCount :int32]]}
     :CheckCollisionLines {:rettype :int8 :argtypes [[startPos1 :vec2] [endPos1 :vec2] [startPos2 :vec2] [endPos2 :vec2] [collisionPoint :pointer]]}
     :CheckCollisionPointLine {:rettype :int8 :argtypes [[point :vec2] [p1 :vec2] [p2 :vec2] [threshold :int32]]}
     :GetCollisionRec {:rettype :rectangle :argtypes [[rec1 :rectangle] [rec2 :rectangle]]}
                                        ;   :texture Loading and Drawing Functions  {:rettype :;// :rettype }[Module: textures]
     ;;//------------------------------------------------------------------------------------
     ;;
     ;;// :image loading functions
     ;;// NOTE: These functions do not require GPU access
     :LoadImage {:rettype :image :argtypes [[fileName :pointer]]}
     :LoadImageRaw {:rettype :image :argtypes [[fileName :pointer] [width :int32] [height :int32] [format :int32] [headerSize :int32]]}
     :LoadImageAnim {:rettype :image :argtypes [[fileName :pointer] [frames :pointer]]}
     :LoadImageFromMemory {:rettype :image :argtypes [[fileType :pointer] [fileData :pointer] [dataSize :int32]]}
     :LoadImageFromTexture {:rettype :image :argtypes [[texture :texture]]}
     :LoadImageFromScreen {:rettype :image :argtypes []}
     :IsImageReady {:rettype :int8 :argtypes [[image :image]]}
     :UnloadImage {:rettype :void :argtypes [[image :image]]}
     :ExportImage {:rettype :int8 :argtypes [[image :image] [fileName :pointer]]}
     :ExportImageToMemory {:rettype :pointer :argtypes [[image :image] [fileType :pointer] [fileSize :pointer]]}
     :ExportImageAsCode {:rettype :int8 :argtypes [[image :image] [fileName :pointer]]}
     ;; generation functions
     :GenImageColor {:rettype :image :argtypes [[width :int32] [height :int32] [color :color]]}
     :GenImageGradientLinear {:rettype :image :argtypes [[width :int32] [height :int32] [direction :int32] [start :color] [end :color]]}
     :GenImageGradientRadial {:rettype :image :argtypes [[width :int32] [height :int32] [density :float32] [inner :color] [outer :color]]}
     :GenImageGradientSquare {:rettype :image :argtypes [[width :int32] [height :int32] [density :float32] [inner :color] [outer :color]]}
     :GenImageChecked {:rettype :image :argtypes [[width :int32] [height :int32] [checksX :int32] [checksY :int32] [col1 :color] [col2 :color]]}
     :GenImageWhiteNoise {:rettype :image :argtypes [[width :int32] [height :int32] [factor :float32]]}
     :GenImagePerlinNoise {:rettype :image :argtypes [[width :int32] [height :int32] [offsetX :int32] [offsetY :int32] [scale :float32]]}
     :GenImageCellular {:rettype :image :argtypes [[width :int32] [height :int32] [tileSize :int32]]}
     :GenImageText {:rettype :image :argtypes [[width :int32] [height :int32] [text :pointer]]}

     :ImageCopy {:rettype :image :argtypes [[image :image]]}
     :ImageFromImage {:rettype :image :argtypes [[image :image] [rec :rectangle]]}
     :ImageText {:rettype :image :argtypes [[text :pointer] [fontSize :int32] [color :color]]}
     :ImageTextEx {:rettype :image :argtypes [[font :font] [text :pointer] [fontSize :float32] [spacing :float32] [tint :color]]}
     :ImageFormat {:rettype :void :argtypes [[image :pointer] [newFormat :int32]]}
     :ImageToPOT {:rettype :void :argtypes [[image :pointer] [fill :color]]}
     :ImageCrop {:rettype :void :argtypes [[image :pointer] [crop :rectangle]]}
     :ImageAlphaCrop {:rettype :void :argtypes [[image :pointer] [threshold :float32]]}
     :ImageAlphaClear {:rettype :void :argtypes [[image :pointer] [color :color] [threshold :float32]]}
     :ImageAlphaMask {:rettype :void :argtypes [[image :pointer] [alphaMask :image]]}
     :ImageAlphaPremultiply {:rettype :void :argtypes [[image :pointer]]}
     :ImageBlurGaussian {:rettype :void :argtypes [[image :pointer] [blurSize :int32]]}
     :ImageResize {:rettype :void :argtypes [[image :pointer] [newWidth :int32] [newHeight :int32]]}
     :ImageResizeNN {:rettype :void :argtypes [[image :pointer] [newWidth :int32] [newHeight :int32]]}
     :ImageResizeCanvas {:rettype :void :argtypes [[image :pointer] [newWidth :int32] [newHeight :int32] [offsetX :int32] [offsetY :int32] [fill :color]]}
     :ImageMipmaps {:rettype :void :argtypes [[image :pointer]]}
     :ImageDither {:rettype :void :argtypes [[image :pointer] [rBpp :int32] [gBpp :int32] [bBpp :int32] [aBpp :int32]]}
     :ImageFlipVertical {:rettype :void :argtypes [[image :pointer]]}
     :ImageFlipHorizontal {:rettype :void :argtypes [[image :pointer]]}
     :ImageRotate {:rettype :void :argtypes [[image :pointer] [degrees :int32]]}
     :ImageRotateCW {:rettype :void :argtypes [[image :pointer]]}
     :ImageRotateCCW {:rettype :void :argtypes [[image :pointer]]}
     :ImageColorTint {:rettype :void :argtypes [[image :pointer] [color :color]]}
     :ImageColorInvert {:rettype :void :argtypes [[image :pointer]]}
     :ImageColorGrayscale {:rettype :void :argtypes [[image :pointer]]}
     :ImageColorContrast {:rettype :void :argtypes [[image :pointer] [contrast :float32]]}
     :ImageColorBrightness {:rettype :void :argtypes [[image :pointer] [brightness :int32]]}
     :ImageColorReplace {:rettype :void :argtypes [[image :pointer] [color :color] [replace :color]]}
     :LoadImageColors {:rettype :pointer :argtypes [[image :image]]}
     :LoadImagePalette {:rettype :pointer :argtypes [[image :image] [maxPaletteSize :int32] [colorCount :pointer]]}
     :UnloadImageColors {:rettype :void :argtypes [[colors :pointer]]}
     :UnloadImagePalette {:rettype :void :argtypes [[colors :pointer]]}
     :GetImageAlphaBorder {:rettype :rectangle :argtypes [[image :image] [threshold :float32]]}
     :GetImageColors {:rettype :color :argtypes [[image :image] [x :int32] [y :int32]]}
     ;; drawing functions
     ;;// NOTE: :image software-rendering functions (CPU)
     :ImageClearBackground {:rettype :void :argtypes [[image :pointer] [color :color]]}
     :ImageDrawPixel {:rettype :void :argtypes [[image :pointer] [posX :int32] [posY :int32] [color :color]]}
     :ImageDrawPixelV {:rettype :void :argtypes [[image :pointer] [position :vec2] [color :color]]}
     :ImageDrawLine {:rettype :void :argtypes [[image :pointer] [startPosX :int32] [startPosY :int32] [endPosX :int32] [endPosY :int32] [color :color]]}
     :ImageDrawLineV {:rettype :void :argtypes [[image :pointer] [start :vec2] [end :vec2] [color :color]]}
     :ImageDrawCircle {:rettype :void :argtypes [[image :pointer] [centerX :int32] [centerY :int32] [radius :int32] [color :color]]}
     :ImageDrawCircleV {:rettype :void :argtypes [[image :pointer] [center :vec2] [radius :int32] [color :color]]}
     :ImageDrawCircleLines {:rettype :void :argtypes [[image :pointer] [centerX :int32] [centerY :int32] [radius :int32] [color :color]]}
     :ImageDrawCircleLinesV {:rettype :void :argtypes [[image :pointer] [center :vec2] [radius :int32] [color :color]]}
     :ImageDrawRectangle {:rettype :void :argtypes [[image :pointer] [posX :int32] [posY :int32] [width :int32] [height :int32] [color :color]]}
     :ImageDrawRectangleV {:rettype :void :argtypes [[image :pointer] [position :vec2] [size :vec2] [color :color]]}
     :ImageDrawRectangleRec {:rettype :void :argtypes [[image :pointer] [rec :rectangle] [color :color]]}
     :ImageDrawRectangleLines {:rettype :void :argtypes [[image :pointer] [rec :rectangle] [thick :int32] [color :color]]}
     :ImageDraw {:rettype :void :argtypes [[image :pointer] [src :image] [srcRec :rectangle] [dstRec :rectangle] [tint :color]]}
     :ImageDrawText {:rettype :void :argtypes [[image :pointer] [text :pointer] [posX :int32] [posY :int32] [fontSize :int32] [color :color]]}
     :ImageDrawTextEx {:rettype :void :argtypes [[image :pointer] [font :font] [text :pointer] [position :vec2] [fontSize :float32] [spacing :float32] [tint :color]]}
     ;; loading functions
     ;;// NOTE: These functions require GPU access
     :LoadTexture {:rettype :texture :argtypes [[fileName :pointer]]}
     :LoadTextureFrom:image {:rettype :texture :argtypes [[image :image]]}
     :LoadTextureCubemap {:rettype :texture :argtypes [[image :image] [layout :int32]]}
     :LoadRenderTexture {:rettype :render-texture :argtypes [[width :int32] [height :int32]]}
     :IsTextureReady {:rettype :int8 :argtypes [[texture :texture]]}
     :UnloadTexture {:rettype :void :argtypes [[texture :texture]]}
     :IsRenderTextureReady {:rettype :int8 :argtypes [[target :render-texture]]}
     :UnloadRenderTexture {:rettype :void :argtypes [[target :render-texture]]}
     :UpdateTexture {:rettype :void :argtypes [[texture :texture] [pixels :pointer]]}
     :UpdateTextureRec {:rettype :void :argtypes [[texture :texture] [rec :rectangle] [pixels :pointer]]}
     ;; configuration functions
     :GenTextureMipmaps {:rettype :void :argtypes [[texture :pointer]]}
     :SetTextureFilter {:rettype :void :argtypes [[texture :texture] [filter :int32]]}
     :SetTextureWrap {:rettype :void :argtypes [[texture :texture] [wrap :int32]]}
     ;; drawing functions
     :DrawTexture {:rettype :void :argtypes [[texture :texture] [posX :int32] [posY :int32] [tint :color]]}
     :DrawTextureV {:rettype :void :argtypes [[texture :texture] [position :vec2] [tint :color]]}
     :DrawTextureEx {:rettype :void :argtypes [[texture :texture] [position :vec2] [rotation :float32] [scale :float32] [tint :color]]}
     :DrawTextureRec {:rettype :void :argtypes [[texture :texture] [source :rectangle] [position :vec2] [tint :color]]}
     :DrawTexturePro {:rettype :void :argtypes [[texture :texture] [source :rectangle] [dest :rectangle] [origin :vec2] [rotation :float32] [tint :color]]}
     :DrawTextureNPatch {:rettype :void :argtypes [[texture :texture] [nPatchInfo :n-patch-info] [dest :rectangle] [origin :vec2] [rotation :float32] [tint :color]]}

     :Fade {:rettype :color :argtypes [[color :color] [alpha :float32]]}
     :ColorToInt {:rettype :int32 :argtypes [[color :color]]}
     :ColorNormalize {:rettype :vec4 :argtypes [[color :color]]}
     :ColorFromNormalized {:rettype :color :argtypes [[normalized :vec4]]}
     :ColorToHSV {:rettype :vec3 :argtypes [[color :color]]}
     :ColorFromHSV {:rettype :color :argtypes [[hue :float32] [saturation :float32] [value :float32]]}
     :ColorTint {:rettype :color :argtypes [[color :color] [tint :color]]}
     :ColorBrightness {:rettype :color :argtypes [[color :color] [factor :float32]]}
     :ColorContrast {:rettype :color :argtypes [[color :color] [contrast :float32]]}
     :ColorAlpha {:rettype :color :argtypes [[color :color] [alpha :float32]]}
     :ColorAlphaBlend {:rettype :color :argtypes [[dst :color] [src :color] [tint :color]]}
     :GetColor {:rettype :color :argtypes [[hexValue :int32]]}
     :GetPixelColor {:rettype :color :argtypes [[srcPtr :pointer] [format :int32]]}
     :SetPixelColor {:rettype :void :argtypes [[dstPtr :pointer] [color :color] [format :int32]]}
     :GetPixelDataSize {:rettype :int32 :argtypes [[width :int32] [height :int32] [format :int32]]}
                                        ;:font Loading and Text Drawing Functions  {:rettype :;// :argtypes }[Module: text]
     ;;//------------------------------------------------------------------------------------
     ;;
     ;;// :font loading/unloading functions

     :SetTextLineSpacing {:rettype :void :argtypes [[spacing :int32]]}
     :MeasureText {:rettype :int32 :argtypes [[text :pointer] [fontSize :int32]]}
     :MeasureTextEx {:rettype :vec2 :argtypes [[font :font] [text :pointer] [fontSize :float32] [spacing :float32]]}
     :GetGlyphIndex {:rettype :int32 :argtypes [[font :font] [codepoint :int32]]}
     :GetGlyphInfo {:rettype :glyph-info :argtypes [[font :font] [codepoint :int32]]}
     :GetGlyphAtlasRec {:rettype :rectangle :argtypes [[font :font] [codepoint :int32]]}
     ;;
     ;;// Text codepoints management functions (unicode characters)
     :LoadUTF8 {:rettype :pointer :argtypes [[codepoints :pointer] [length :int32]]}
     :UnloadUTF8 {:rettype :void :argtypes [[text :pointer]]}
     :LoadCodepoints {:rettype :pointer :argtypes [[text :pointer] [count :pointer]]}
     :UnloadCodepoints {:rettype :void :argtypes [[codepoints :pointer]]}
     :GetCodepointCount {:rettype :int32 :argtypes [[text :pointer]]}
     :GetCodepoint {:rettype :int32 :argtypes [[text :pointer] [codepointSize :pointer]]}
     :GetCodepointNext {:rettype :int32 :argtypes [[text :pointer] [codepointSize :pointer]]}
     :GetCodepointPrevious {:rettype :int32 :argtypes [[text :pointer] [codepointSize :pointer]]}
     :CodepointToUTF8 {:rettype :pointer :argtypes [[codepoint :int32] [utf8Size :pointer]]}
     ;;
     ;;// Text strings management functions (no UTF-8 strings, only byte chars)
     ;;// NOTE: Some strings allocate memory internally for returned strings, just be careful!
     :TextCopy {:rettype :int32 :argtypes [[dst :pointer] [src :pointer]]}
     :TextIsEqual {:rettype :int8 :argtypes [[text1 :pointer] [text2 :pointer]]}
     :TextLength {:rettype :int32 :argtypes [[text :pointer]]}
                                        ;TODO: what to do here?
                                        ;:TextFormat {:rettype :pointer :argtypes [[text :pointer] ...]}
     :TextSubtext {:rettype :pointer :argtypes [[text :pointer] [position :int32] [length :int32]]}
     :TextReplace {:rettype :pointer :argtypes [[text :pointer] [replace :pointer] [by :pointer]]}
     :TextInsert {:rettype :pointer :argtypes [[text :pointer] [insert :pointer] [position :int32]]}
     :TextJoin {:rettype :pointer :argtypes [[textList :pointer] [count :int32] [delimiter :pointer]]}
     :TextSplit {:rettype :pointer :argtypes [[text :pointer] [delimiter :int8] [count :pointer]]}
     :TextAppend {:rettype :void :argtypes [[text :pointer] [append :pointer] [position :pointer]]}
     :TextFindIndex {:rettype :int32 :argtypes [[text :pointer] [find :pointer]]}
     :TextToUpper {:rettype :pointer :argtypes [[text :pointer]]}
     :TextToLower {:rettype :pointer :argtypes [[text :pointer]]}
     :TextToPascal {:rettype :pointer :argtypes [[text :pointer]]}
     :TextToInteger {:rettype :int32 :argtypes [[text :pointer]]}
     ;;// Basic 3d Shapes Drawing Functions (Module: models)
     ;;//------------------------------------------------------------------------------------
     ;;
     ;;// Basic geometric 3D shapes drawing functions
     :DrawLine3D {:rettype :void :argtypes [[startPos :vec3] [endPos :vec3] [color :color]]}
     :DrawPoint3D {:rettype :void :argtypes [[position :vec3] [color :color]]}
     :DrawCircle3D {:rettype :void :argtypes [[center :vec3] [radius :float32] [rotationAxis :vec3] [rotationAngle :float32] [color :color]]}
     :DrawTriangle3D {:rettype :void :argtypes [[v1 :vec3] [v2 :vec3] [v3 :vec3] [color :color]]}
     :DrawTriangleStrip3D {:rettype :void :argtypes [[points :pointer] [pointCount :int32] [color :color]]}
     :DrawCube {:rettype :void :argtypes [[position :vec3] [width :float32] [height :float32] [length :float32] [color :color]]}
     :DrawCubeV {:rettype :void :argtypes [[position :vec3] [size :vec3] [color :color]]}
     :DrawCubeWires {:rettype :void :argtypes [[position :vec3] [width :float32] [height :float32] [length :float32] [color :color]]}
     :DrawCubeWiresV {:rettype :void :argtypes [[position :vec3] [size :vec3] [color :color]]}
     :DrawSphere {:rettype :void :argtypes [[centerPos :vec3] [radius :float32] [color :color]]}
     :DrawSphereEx {:rettype :void :argtypes [[centerPos :vec3] [radius :float32] [rings :int32] [slices :int32] [color :color]]}
     :DrawSphereWires {:rettype :void :argtypes [[centerPos :vec3] [radius :float32] [rings :int32] [slices :int32] [color :color]]}
     :DrawCylinder {:rettype :void :argtypes [[position :vec3] [radiusTop :float32] [radiusBottom :float32] [height :float32] [slices :int32] [color :color]]}
     :DrawCylinderEx {:rettype :void :argtypes [[startPos :vec3] [endPos :vec3] [startRadius :float32] [endRadius :float32] [sides :int32] [color :color]]}
     :DrawCylinderWires {:rettype :void :argtypes [[position :vec3] [radiusTop :float32] [radiusBottom :float32] [height :float32] [slices :int32] [color :color]]}
     :DrawCylinderWiresEx {:rettype :void :argtypes [[startPos :vec3] [endPos :vec3] [startRadius :float32] [endRadius :float32] [sides :int32] [color :color]]}
     :DrawCapsule {:rettype :void :argtypes [[startPos :vec3] [endPos :vec3] [radius :float32] [slices :int32] [rings :int32] [color :color]]}
     :DrawCapsuleWires {:rettype :void :argtypes [[startPos :vec3] [endPos :vec3] [radius :float32] [slices :int32] [rings :int32] [color :color]]}
     :DrawPlane {:rettype :void :argtypes [[centerPos :vec3] [size :vec2] [color :color]]}
     :DrawRay {:rettype :void :argtypes [[ray :ray] [color :color]]}
     :DrawGrid {:rettype :void :argtypes [[slices :int32] [spacing :float32]]}
     ;;// :model 3d Loading and Drawing Functions (Module: models)
     ;;//------------------------------------------------------------------------------------
     ;;
     ;;// :model management functions
     :LoadModel {:rettype :model :argtypes [[fileName :pointer]]}
     :LoadModelFromMesh {:rettype :model :argtypes [[mesh :mesh]]}
     :IsModelReady {:rettype :int8 :argtypes [[model :model]]}
     :UnloadModel {:rettype :void :argtypes [[model :model]]}
     :GetModelBoundingBox {:rettype :bounding-box :argtypes [[model :model]]}

     :DrawModel {:rettype :void :argtypes [[model :model] [position :vec3] [scale :float32] [tint :color]]}
     :DrawModelEx {:rettype :void :argtypes [[model :model] [position :vec3] [rotationAxis :vec3] [rotationAngle :float32] [scale :vec3] [tint :color]]}
     :DrawModelWires {:rettype :void :argtypes [[model :model] [position :vec3] [scale :float32] [tint :color]]}
     :DrawModelWiresEx {:rettype :void :argtypes [[model :model] [position :vec3] [rotationAxis :vec3] [rotationAngle :float32] [scale :vec3] [tint :color]]}
     :DrawBoundingBox {:rettype :void :argtypes [[box :bounding-box] [color :color]]}
     :DrawBillboard {:rettype :void :argtypes [[camera :camera-3d] [texture :texture] [position :vec3] [size :float32] [tint :color]]}
     :DrawBillboardRec {:rettype :void :argtypes [[camera :camera-3d] [texture :texture] [source :rectangle] [position :vec3] [size :vec2] [tint :color]]}
     :DrawBillboardPro {:rettype :void :argtypes [[camera :camera-3d] [texture :texture] [source :rectangle] [position :vec3] [up :vec3] [size :vec2] [origin :vec2] [rotation :float32] [tint :color]]}
     ;;
     ;;// :mesh management functions
     :UploadMesh {:rettype :void :argtypes [[mesh :pointer] [dynamic :int8]]}
     :UpdateMeshBuffer {:rettype :void :argtypes [[mesh :mesh] [index :int32] [data :pointer] [dataSize :int32] [offset :int32]]}
     :UnloadMesh {:rettype :void :argtypes [[mesh :mesh]]}
     :DrawMesh {:rettype :void :argtypes [[mesh :mesh] [material :material] [transform :mat4]]}
     :DrawMeshInstanced {:rettype :void :argtypes [[mesh :mesh] [material :material] [transforms :pointer] [instances :int32]]}
     :ExportMesh {:rettype :int8 :argtypes [[mesh :mesh] [fileName :pointer]]}
     :GetMeshBoundingBox {:rettype :bounding-box :argtypes [[mesh :mesh]]}
     :GenMeshTangents {:rettype :void :argtypes [[mesh :pointer]]}
     ;; generation functions
     :GenMeshPoly {:rettype :mesh :argtypes [[sides :int32] [radius :float32]]}
     :GenMeshPlane {:rettype :mesh :argtypes [[width :float32] [length :float32] [resX :int32] [resZ :int32]]}
     :GenMeshCube {:rettype :mesh :argtypes [[width :float32] [height :float32] [length :float32]]}
     :GenMeshSphere {:rettype :mesh :argtypes [[radius :float32] [rings :int32] [slices :int32]]}
     :GenMeshHemiSphere {:rettype :mesh :argtypes [[radius :float32] [rings :int32] [slices :int32]]}
     :GenMeshCylinder {:rettype :mesh :argtypes [[radius :float32] [height :float32] [slices :int32]]}
     :GenMeshCone {:rettype :mesh :argtypes [[radius :float32] [height :float32] [slices :int32]]}
     :GenMeshTorus {:rettype :mesh :argtypes [[radius :float32] [size :float32] [radSeg :int32] [sides :int32]]}
     :GenMeshKnot {:rettype :mesh :argtypes [[radius :float32] [size :float32] [radSeg :int32] [sides :int32]]}
     :GenMeshHeightmap {:rettype :mesh :argtypes [[heightmap :image] [size :vec3]]}
     :GenMeshCubicmap {:rettype :mesh :argtypes [[cubicmap :image] [cubeSize :vec3]]}
     ;;

     }
   nil ;;no library symbols defined
   nil ;;no systematic error checking
   ))


(comment
  (ffi/define-library!
   native-part-2
   '{
     ;;// :material loading/unloading functions
     :LoadMaterials {:rettype :pointer :argtypes [[fileName :pointer] [materialCount :pointer]]}
     :LoadMaterialDefault {:rettype :material :argtypes []}
     :IsMaterialReady {:rettype :int8 :argtypes [[material :material]]}
     :UnloadMaterial {:rettype :void :argtypes [[material :material]]}
     :SetMaterialTexture {:rettype :void :argtypes [[material :pointer] [mapType :int32] [texture :texture]]}
     :SetModelMeshMaterial {:rettype :void :argtypes [[model :pointer] [meshId :int32] [materialId :int32]]}
     ;;
     ;;// :model animations loading/unloading functions
     :LoadModelAnimations {:rettype :pointer :argtypes [[fileName :pointer] [animCount :pointer]]}
     :UpdateModelAnimation {:rettype :void :argtypes [[model :model] [anim :model-animation] [frame :int32]]}
     :UnloadModelAnimation {:rettype :void :argtypes [[anim :model-animation]]}
     :UnloadModelAnimations {:rettype :void :argtypes [[animations :pointer] [count :int32]]}
     :IsModelAnimationValid {:rettype :int8 :argtypes [[model :model] [anim :model-animation]]}

     :CheckCollisionSpheres {:rettype :int8 :argtypes [[center1 :vec3] [radius1 :float32] [center2 :vec3] [radius2 :float32]]}
     :CheckCollisionBoxes {:rettype :int8 :argtypes [[box1 :bounding-box] [box2 :bounding-box]]}
     :CheckCollisionBoxSphere {:rettype :int8 :argtypes [[box :bounding-box] [center :vec3] [radius :float32]]}
     :GetRayCollisionSphere {:rettype :ray-collision :argtypes [[ray :ray] [center :vec3] [radius :float32]]}
     :GetRayCollisionBox {:rettype :ray-collision :argtypes [[ray :ray] [box :bounding-box]]}
     :GetRayCollision:mesh {:rettype :ray-collision :argtypes [[ray :ray] [mesh :mesh] [transform :mat4]]}
     :GetRayCollisionTriangle {:rettype :ray-collision :argtypes [[ray :ray] [p1 :vec3] [p2 :vec3] [p3 :vec3]]}
     :GetRayCollisionQuad {:rettype :ray-collision :argtypes [[ray :ray] [p1 :vec3] [p2 :vec3] [p3 :vec3] [p4 :vec3]]}
     ;;// Audio Loading and Playing Functions (Module: audio)
     ;;//------------------------------------------------------------------------------------
     ;;typedef void (*AudioCallback)(:pointer bufferData, :int32 frames);
     ;;
     ;;// Audio device management functions
     :InitAudioDevice {:rettype :void :argtypes []}
     :CloseAudioDevice {:rettype :void :argtypes []}
     :IsAudioDeviceReady {:rettype :int8 :argtypes []}
     :SetMasterVolume {:rettype :void :argtypes [[volume :float32]]}

     ;;// Wave/:sound loading/unloading functions
     :LoadWave {:rettype :wave :argtypes [[fileName :pointer]]}
     :LoadWaveFromMemory {:rettype :wave :argtypes [[fileType :pointer] [fileData :pointer] [dataSize :int32]]}
     :IsWaveReady {:rettype :int8 :argtypes [[wave :wave]]}
     :LoadSound {:rettype :sound :argtypes [[fileName :pointer]]}
     :LoadSoundFromWave {:rettype :sound :argtypes [[wave :wave]]}
     :LoadSoundAlias {:rettype :sound :argtypes [[source :sound]]}
     :IsSoundReady {:rettype :int8 :argtypes [[sound :sound]]}
     :UpdateSound {:rettype :void :argtypes [[sound :sound]  [data :pointer] [sampleCount :int32]]}
     :UnloadWave {:rettype :void :argtypes [[wave :wave]]}
     :UnloadSound {:rettype :void :argtypes [[sound :sound]]}
     :UnloadSoundAlias {:rettype :void :argtypes [[alias :sound]]}
     :ExportWave {:rettype :int8 :argtypes [[wave :wave] [fileName :pointer]]}
     :ExportWaveAsCode {:rettype :int8 :argtypes [[wave :wave] [fileName :pointer]]}
     ;; management functions
     :PlaySound {:rettype :void :argtypes [[sound :sound]]}
     :StopSound {:rettype :void :argtypes [[sound :sound]]}
     :PauseSound {:rettype :void :argtypes [[sound :sound]]}
     :ResumeSound {:rettype :void :argtypes [[sound :sound]]}
     :IsSoundPlaying {:rettype :int8 :argtypes [[sound :sound]]}
     :SetSoundVolume {:rettype :void :argtypes [[sound :sound] [volume :float32]]}
     :SetSoundPitch {:rettype :void :argtypes [[sound :sound] [pitch :float32]]}
     :SetSoundPan {:rettype :void :argtypes [[sound :sound] [pan :float32]]}
     :WaveCopy {:rettype :wave :argtypes [[wave :wave]]}
     :WaveCrop {:rettype :void :argtypes [[wave :pointer] [initSample :int32] [finalSample :int32]]}
     :WaveFormat {:rettype :void :argtypes [[wave :pointer] [sampleRate :int32] [sampleSize :int32] [channels :int32]]}
     :LoadWaveSamples {:rettype :pointer :argtypes [[wave :wave]]}
     :UnloadWaveSamples {:rettype :void :argtypes [[samples :pointer]]}

     :LoadMusicStream {:rettype :music :argtypes [[fileName :pointer]]}
     :LoadMusicStreamFromMemory {:rettype :music :argtypes [[fileType :pointer] [data :pointer] [dataSize :int32]]}
     :IsMusicReady {:rettype :int8 :argtypes [[music :music]]}
     :UnloadMusicStream {:rettype :void :argtypes [[music :music]]}
     :PlayMusicStream {:rettype :void :argtypes [[music :music]]}
     :IsMusicStreamPlaying {:rettype :int8 :argtypes [[music :music]]}
     :UpdateMusicStream {:rettype :void :argtypes [[music :music]]}
     :StopMusicStream {:rettype :void :argtypes [[music :music]]}
     :PauseMusicStream {:rettype :void :argtypes [[music :music]]}
     :ResumeMusicStream {:rettype :void :argtypes [[music :music]]}
     :SeekMusicStream {:rettype :void :argtypes [[music :music] [position :float32]]}
     :SetMusicVolume {:rettype :void :argtypes [[music :music] [volume :float32]]}
     :SetMusicPitch {:rettype :void :argtypes [[music :music] [pitch :float32]]}
     :SetMusicPan {:rettype :void :argtypes [[music :music] [pan :float32]]}
     :GetMusicTimeLength {:rettype :float :argtypes [[music :music]]}
     :GetMusicTimePlayed {:rettype :float :argtypes [[music :music]]}
     ;; management functions
     :LoadAudioStream {:rettype :audio-stream :argtypes [[sampleRate :int32] [sampleSize :int32] [channels :int32]]}
     :IsAudioStreamReady {:rettype :int8 :argtypes [[stream :audio-stream]]}
     :UnloadAudioStream {:rettype :void :argtypes [[stream :audio-stream]]}
     :UpdateAudioStream {:rettype :void :argtypes [[stream :audio-stream] [data :pointer] [frameCount :int32]]}
     :IsAudioStreamProcessed {:rettype :int8 :argtypes [[stream :audio-stream]]}
     :PlayAudioStream {:rettype :void :argtypes [[stream :audio-stream]]}
     :PauseAudioStream {:rettype :void :argtypes [[stream :audio-stream]]}
     :ResumeAudioStream {:rettype :void :argtypes [[stream :audio-stream]]}
     :IsAudioStreamPlaying {:rettype :int8 :argtypes [[stream :audio-stream]]}
     :StopAudioStream {:rettype :void :argtypes [[stream :audio-stream]]}
     :SetAudioStreamVolume {:rettype :void :argtypes [[stream :audio-stream] [volume :float32]]}
     :SetAudioStreamPitch {:rettype :void :argtypes [[stream :audio-stream] [pitch :float32]]}
     :SetAudioStreamPan {:rettype :void :argtypes [[stream :audio-stream] [pan :float32]]}
     :SetAudioStreamBufferSizeDefault {:rettype :void :argtypes [[size :int32]]}
                                        ;TODO: how to handle callback?
                                        ;:SetAudioStreamCallback {:rettype :void :argtypes [[stream :audio-stream] [callback AudioCallback]]}
                                        ;:AttachAudioStreamProcessor {:rettype :void :argtypes [[stream :audio-stream] [processor AudioCallback]]}
                                        ;:DetachAudioStreamProcessor {:rettype :void :argtypes [[stream :audio-stream] [processor AudioCallback]]}
                                        ;:AttachAudioMixedProcessor {:rettype :void :argtypes [[processor AudioCallback]]}
                                        ;:DetachAudioMixedProcessor {:rettype :void :argtypes [[processor AudioCallback]]}

     }
   nil
   nil
   ))

;(ffi/set-ffi-impl! :jdk)

;(ffi/library-singleton-set! native-part-1 "raylib")

;(ffi/library-singleton-set! native-part-2 "raylib")


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

