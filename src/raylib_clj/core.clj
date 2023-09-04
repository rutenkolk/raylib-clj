(ns raylib-clj.core
  (:gen-class)
  (:require
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

;deprecated?
(defn as-signed-char  [v] (- v (* (if (< v 128) 0 1) 256)))
(defn as-signed-short [v] (- v (* (if (< v 16384) 0 1) 32768)))
(defn as-signed-int   [v] (- v (* (if (< v 1073741824) 0 1) 2147483648)))
(defn as-boolean-int  [v] (if v 1 0))
;deprecated?
(def value-as-type
  {'uchar 'as-signed-char
   'ui8   'as-signed-char
   'ui16  'as-signed-short
   'ui32  'as-signed-int
   'bool  'as-boolean-int
   })

;deprecated?
(defn get-data-from-datatype [index [type-name member-name]]
  (let [value-as-type-fn (value-as-type type-name)]
    (if value-as-type-fn
      [member-name `(~(value-as-type type-name) (nth ~'obj ~index))]
      [member-name `(nth ~'obj ~index)])))

(defn- duplicate-member-name-as-keyword-and-symbol [in]
  (let [[member-name _] (eval in)] [member-name (symbol (name member-name))]))

(defmacro serialize-into-with-vector [type-name members]
  `(defmethod mem/serialize-into ~(keyword "raylib-clj.core" (name type-name))
     [~'obj ~'_ ~'segment ~'session]
     (let [~'serializing-map
           (if (vector? ~'obj)
             (let [~(->> members (map (comp symbol name first eval)) (vec)) ~'obj]
               ~(->> members (map duplicate-member-name-as-keyword-and-symbol) (into (hash-map))))
             ~'obj)]
         (mem/serialize-into
       ~'serializing-map
       (layout/with-c-layout
         [::mem/struct ~(eval members)])
       ~'segment
       ~'session))))

(defmacro define-datatype! [type-name members]
  `(mem/defalias ~(keyword "raylib-clj.core" (name type-name))
    (layout/with-c-layout
      [::mem/struct ~members])))

(mem/defalias ::bool [::mem/struct [[:value ::mem/byte]]])

(defmethod mem/serialize-into ::bool
  [v _ segment session]
  (mem/serialize-into
   {:value (if v 1 0)}
   [::mem/struct [[:value ::mem/byte]]]
   segment
   session))

(defmethod mem/deserialize-from ::bool
  [segment _]
  (let [byteval (mem/deserialize-from segment ::mem/byte)]
    (not= 0 byteval)))

(define-datatype! :vec2 [(f32 :x) (f32 :y)])
(define-datatype! :vec3 [(f32 :x) (f32 :y) (f32 :z)])
(define-datatype! :vec4 [(f32 :x) (f32 :y) (f32 :z) (f32 :w)])

(serialize-into-with-vector :vec2 [(f32 :x) (f32 :y)])
(serialize-into-with-vector :vec3 [(f32 :x) (f32 :y) (f32 :z)])
(serialize-into-with-vector :vec4 [(f32 :x) (f32 :y) (f32 :z) (f32 :w)])

;alias quaternion as vec4?

(define-datatype! :mat4
  [(f32 :m0)(f32 :m4)(f32 :m8) (f32 :m12)
   (f32 :m1)(f32 :m5)(f32 :m9) (f32 :m13)
   (f32 :m2)(f32 :m6)(f32 :m10)(f32 :m14)
   (f32 :m3)(f32 :m7)(f32 :m11)(f32 :m15)])

(serialize-into-with-vector
 :mat4
 [(f32 :m0)(f32 :m4)(f32 :m8) (f32 :m12)
  (f32 :m1)(f32 :m5)(f32 :m9) (f32 :m13)
  (f32 :m2)(f32 :m6)(f32 :m10)(f32 :m14)
  (f32 :m3)(f32 :m7)(f32 :m11)(f32 :m15)])

(define-datatype! :color [(uchar :r) (uchar :g) (uchar :b) (uchar :a)])
(serialize-into-with-vector :color [(uchar :r) (uchar :g) (uchar :b) (uchar :a)])

(define-datatype! :rectangle [(f32 :x) (f32 :y) (f32 :width) (f32 :height)])
(serialize-into-with-vector :rectangle [(f32 :x) (f32 :y) (f32 :width) (f32 :height)])

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
;(def BLACK      [ 0, 0, 0, 255          ])
(def BLACK      {:r 0 :g 0 :b 0 :a 255})

(def BLANK      [ 0, 0, 0, 0            ])
(def MAGENTA    [ 255, 0, 255, 255      ])

;(def RAYWHITE   [ 245, 245, 245, 255    ])
(def RAYWHITE    {:r 245 :g 245 :b 245 :a 255})

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
  {:int8        ::mem/byte
   :int16       ::mem/short
   :int32       ::mem/int
   :uint8       ::mem/byte
   :uint16      ::mem/short
   :uint32      ::mem/int
   :void        ::mem/void
   :ptr         ::mem/pointer
   :pointer     ::mem/pointer
   :float32     ::mem/float
   :float       ::mem/float

   })

(defn type-converter [old]
  (let [new-typename (old-types->new-types old)]
    (cond
      new-typename new-typename
      (vector? old) (recur (second old))
      :else (keyword "raylib-clj.core" (name old))
     )))

(defn arg-decl->arg-name [old]
  (if (vector? old) (str (name (first old)))))

(defn coffify [fn-name-old signature]
  (let [fn-name-new (symbol (csk/->kebab-case (name fn-name-old)))
        rettype (type-converter (:rettype signature))
        argtypes (vec (map type-converter (:argtypes signature)))
        argnames (filter identity (map arg-decl->arg-name (:argtypes signature)))
        argnames-str (clojure.string/join " " argnames)
        boolean-return? (and (clojure.string/starts-with? fn-name-new "is-") (= rettype ::mem/byte))
        cfn-name-final (if boolean-return?
                         (-> fn-name-new
                             (str)
                             (clojure.string/replace-first "is-" "")
                             (str "?")
                             (symbol))
                         fn-name-new)
        ]
    `(cffi/defcfn ~cfn-name-final
       ~(str "[" argnames-str "]" " -> " (if boolean-return? "bool" (name rettype)))
       ~(symbol (name fn-name-old))
       ~argtypes
       ~rettype
       )
    )
  )

(coffi.ffi/defcfn
  window-should-close?
  "[] -> bool"
  WindowShouldClose
  []
  :raylib-clj.core/bool
  )

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
  ::bool)

(coffi.ffi/defcfn
  window-fullscreen?
  "[ ] -> bool"
  IsWindowFullscreen
  []
  ::bool)

(coffi.ffi/defcfn
  window-hidden?
  "[ ] -> bool"
  IsWindowHidden
  []
  ::bool)

(coffi.ffi/defcfn
  window-minimized?
  "[ ] -> bool"
  IsWindowMinimized
  []
  ::bool)

(coffi.ffi/defcfn
  window-maximized?
  "[ ] -> bool"
  IsWindowMaximized
  []
  ::bool)

(coffi.ffi/defcfn
  window-focused?
  "[] -> bool"
  IsWindowFocused
  []
  ::bool)

(coffi.ffi/defcfn
  window-resized?
  "[] -> bool"
  IsWindowResized
  []
  ::bool)

(coffi.ffi/defcfn
  window-state?
  "[flag] -> bool"
  IsWindowState
  [:coffi.mem/int]
  ::bool)

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

(coffi.ffi/defcfn
  toggle-borderless-windowed
  "[] -> void"
  ToggleBorderlessWindowed
  []
  :coffi.mem/void)

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
(coffi.ffi/defcfn
  set-window-opacity
  "[opacity] -> void"
  SetWindowOpacity
  [:coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-window-icons
  "[images] -> void"
  SetWindowIcons
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void
  native-fn [images]
  (with-open [session (mem/stack-session)]
    (let [cnt (count images)
          arr (mem/serialize images [::mem/array ::image cnt] session)
          ptr (mem/address-of arr)]
      (native-fn ptr))))
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
(coffi.ffi/defcfn
  set-window-title
  "[title] -> void"
  SetWindowTitle
  [:coffi.mem/c-string]
  :coffi.mem/void)
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
(coffi.ffi/defcfn
  get-window-handle
  "[] -> pointer"
  GetWindowHandle
  []
  :coffi.mem/pointer)
(coffi.ffi/defcfn
  set-window-icon
  "[image] -> void"
  SetWindowIcon
  [:raylib-clj.core/image]
  :coffi.mem/void)

(coffi.ffi/defcfn
  get-clipboard-text
  "[] -> string"
  GetClipboardText
  []
  :coffi.mem/c-string)
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
(coffi.ffi/defcfn
  get-monitor-physical-width
  "[monitor] -> int"
  GetMonitorPhysicalWidth
  [:coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-monitor-name
  "[monitor] -> string"
  GetMonitorName
  [:coffi.mem/int]
  :coffi.mem/c-string)
(coffi.ffi/defcfn
  set-clipboard-text
  "[text] -> void"
  SetClipboardText
  [:coffi.mem/c-string]
  :coffi.mem/void)

(coffi.ffi/defcfn
  get-clipboard-text
  "[] -> string"
  GetClipboardText
  []
  :coffi.mem/c-string)
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
(coffi.ffi/defcfn
  wait-time
  "[seconds] -> void"
  WaitTime
  [:coffi.mem/double]
  :coffi.mem/void)
(coffi.ffi/defcfn
  begin-mode-2-d
  "[camera] -> void"
  BeginMode2D
  [:raylib-clj.core/camera-2d]
  :coffi.mem/void)
(coffi.ffi/defcfn
  cursor-on-screen?
  "[] -> bool"
  IsCursorOnScreen
  []
  ::bool)
(coffi.ffi/defcfn end-mode-2-d "[] -> void" EndMode2D [] :coffi.mem/void)
(coffi.ffi/defcfn
  clear-background
  "[color] -> void"
  ClearBackground
  [:raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  cursor-hidden?
  "[] -> bool"
  IsCursorHidden
  []
  ::bool)
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




(coffi.ffi/defcfn
  begin-scissor-mode
  "[x y width height] -> void"
  BeginScissorMode
  [:coffi.mem/int :coffi.mem/int :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  end-scissor-mode
  "[] -> void"
  EndScissorMode
  []
  :coffi.mem/void)
(coffi.ffi/defcfn
  begin-vr-stereo-mode
  "[config] -> void"
  BeginVrStereoMode
  [:raylib-clj.core/vr-stereo-config]
  :coffi.mem/void)
(coffi.ffi/defcfn
  end-vr-stereo-mode
  "[] -> void"
  EndVrStereoMode
  []
  :coffi.mem/void)


(coffi.ffi/defcfn
  load-vr-stereo-config
  "[device] -> vr-stereo-config"
  LoadVrStereoConfig
  [:raylib-clj.core/vr-device-info]
  :raylib-clj.core/vr-stereo-config)
(coffi.ffi/defcfn
  unload-vr-stereo-config
  "[config] -> void"
  UnloadVrStereoConfig
  [:raylib-clj.core/vr-stereo-config]
  :coffi.mem/void)

;font stuff 
(coffi.ffi/defcfn
  font-ready?
  "[font] -> bool"
  IsFontReady
  [:raylib-clj.core/font]
  ::bool)
(coffi.ffi/defcfn
  export-font-as-code
  "[font fileName] -> byte"
  ExportFontAsCode
  [:raylib-clj.core/font :coffi.mem/c-string]
  :coffi.mem/byte)
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
  [:coffi.mem/pointer :raylib-clj.core/glyph-info])
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
  [:coffi.mem/c-string]
  :raylib-clj.core/font)
(coffi.ffi/defcfn
  draw-text-pro
  "[font text position origin rotation fontSize spacing tint] -> void"
  DrawTextPro
  [:raylib-clj.core/font
   :coffi.mem/c-string
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
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
  [:coffi.mem/c-string
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
(coffi.ffi/defcfn
  draw-text-codepoints
  "[font codepoints count position fontSize spacing tint] -> void"
  DrawTextCodepoints
  [:raylib-clj.core/font
   :coffi.mem/pointer
   :coffi.mem/int
   :raylib-clj.core/vec2
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-text-ex
  "[font text position fontSize spacing tint] -> void"
  DrawTextEx
  [:raylib-clj.core/font
   :coffi.mem/c-string
   :raylib-clj.core/vec2
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-text-codepoint
  "[font codepoint position fontSize tint] -> void"
  DrawTextCodepoint
  [:raylib-clj.core/font
   :coffi.mem/int
   :raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
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
  [:coffi.mem/c-string
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-font-from-memory
  "[fileType fileData dataSize fontSize fontChars glyphCount] -> font"
  LoadFontFromMemory
  [:coffi.mem/c-string
   :coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int]
  :raylib-clj.core/font)

(def- shader-uniform-vec-type
  {[true 2] SHADER_UNIFORM_IVEC2
   [true 3] SHADER_UNIFORM_IVEC3
   [true 4] SHADER_UNIFORM_IVEC4

   [false 2] SHADER_UNIFORM_VEC2
   [false 3] SHADER_UNIFORM_VEC3
   [false 4] SHADER_UNIFORM_VEC4
   })

(def- shader-uniform-type-mem-layout
  {SHADER_UNIFORM_INT   ::mem/int
   SHADER_UNIFORM_IVEC2 [::mem/array ::mem/int 2]
   SHADER_UNIFORM_IVEC3 [::mem/array ::mem/int 3]
   SHADER_UNIFORM_IVEC4 [::mem/array ::mem/int 4]

   SHADER_UNIFORM_FLOAT ::mem/float
   SHADER_UNIFORM_VEC2  [::mem/array ::mem/float 2]
   SHADER_UNIFORM_VEC3  [::mem/array ::mem/float 3]
   SHADER_UNIFORM_VEC4  [::mem/array ::mem/float 4]

   SHADER_UNIFORM_SAMPLER2D ::mem/int})
(coffi.ffi/defcfn
  set-shader-value
  "[shader loc-index value uniformType] -> void"
  SetShaderValue
  [:raylib-clj.core/shader
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int]
  :coffi.mem/void)
;TODO: how does raylib do the uniform variables?
(coffi.ffi/defcfn
  set-shader-value-convenience
  "[shader int any] -> void"
  SetShaderValue
  [:raylib-clj.core/shader
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int]
  :coffi.mem/void
  native-fn [shader locIndex value]
  (with-open [session (mem/stack-session)]
    (let [uniform-type (cond
                         (vector? value) (shader-uniform-vec-type [(integer? (first value)) (count value)])
                         (integer? value) SHADER_UNIFORM_INT
                         (float? value)   SHADER_UNIFORM_FLOAT
                         :else            SHADER_UNIFORM_SAMPLER2D)
          mem-layout (shader-uniform-type-mem-layout uniform-type)
          value-segment (mem/serialize value mem-layout session)
          value-ptr (mem/address-of value-segment)]
      (native-fn shader locIndex value-ptr uniform-type))))
(coffi.ffi/defcfn
  shader-ready?
  "[shader] -> bool"
  IsShaderReady
  [:raylib-clj.core/shader]
  ::bool)
(coffi.ffi/defcfn
  set-shader-value-matrix
  "[shader locIndex mat] -> mat4"
  SetShaderValueMatrix
  [:raylib-clj.core/shader :coffi.mem/int :raylib-clj.core/mat4]
  :raylib-clj.core/mat4)
(coffi.ffi/defcfn
  unload-shader
  "[shader] -> shader"
  UnloadShader
  [:raylib-clj.core/shader]
  :raylib-clj.core/shader)
(coffi.ffi/defcfn
  set-shader-value-texture
  "[shader locIndex texture] -> texture"
  SetShaderValueTexture
  [:raylib-clj.core/shader :coffi.mem/int :raylib-clj.core/texture]
  :raylib-clj.core/texture)
(coffi.ffi/defcfn
  load-shader-from-memory
  "[vsCode fsCode] -> shader"
  LoadShaderFromMemory
  [:coffi.mem/c-string :coffi.mem/c-string]
  :raylib-clj.core/shader)
(coffi.ffi/defcfn
  load-shader
  "[vsFileName fsFileName] -> shader"
  LoadShader
  [:coffi.mem/c-string :coffi.mem/c-string]
  :raylib-clj.core/shader)
(coffi.ffi/defcfn
  get-shader-location-attrib
  "[shader attribName] -> int"
  GetShaderLocationAttrib
  [:raylib-clj.core/shader :coffi.mem/c-string]
  :coffi.mem/int)
(coffi.ffi/defcfn
  set-shader-value-v
  "[shader locIndex value uniformType count] -> void"
  SetShaderValueV
  [:raylib-clj.core/shader
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-shader-location
  "[shader uniformName] -> int"
  GetShaderLocation
  [:raylib-clj.core/shader :coffi.mem/c-string]
  :coffi.mem/int)


(coffi.ffi/defcfn
  get-mouse-ray
  "[mousePosition camera] -> ray"
  GetMouseRay
  [:raylib-clj.core/vec2 :raylib-clj.core/camera-3d]
  :raylib-clj.core/ray)
(coffi.ffi/defcfn
  get-camera-matrix
  "[camera] -> mat4"
  GetCameraMatrix
  [:raylib-clj.core/camera-3d]
  :raylib-clj.core/mat4)
(coffi.ffi/defcfn
  get-camera-matrix-2-d
  "[camera] -> mat4"
  GetCameraMatrix2D
  [:raylib-clj.core/camera-2d]
  :raylib-clj.core/mat4)
(coffi.ffi/defcfn
  get-world-to-screen
  "[position camera] -> vec2"
  GetWorldToScreen
  [:raylib-clj.core/vec3 :raylib-clj.core/camera-3d]
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  get-screen-to-world-2-d
  "[position camera] -> vec2"
  GetScreenToWorld2D
  [:raylib-clj.core/vec2 :raylib-clj.core/camera-2d]
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  get-world-to-screen-ex
  "[position camera width height] -> vec2"
  GetWorldToScreenEx
  [:raylib-clj.core/vec3
   :raylib-clj.core/camera-3d
   :coffi.mem/int
   :coffi.mem/int]
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  get-world-to-screen-2-d
  "[position camera] -> vec2"
  GetWorldToScreen2D
  [:raylib-clj.core/vec2 :raylib-clj.core/camera-2d]
  :raylib-clj.core/vec2)

(coffi.ffi/defcfn
  set-target-fps
  "[fps] -> void"
  SetTargetFPS
  [:coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  open-url
  "[url] -> void"
  OpenURL
  [:coffi.mem/c-string]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-config-flags
  "[flags] -> void"
  SetConfigFlags
  [:coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  mem-free
  "[ptr] -> void"
  MemFree
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn get-fps "[] -> int" GetFPS [] :coffi.mem/int)
(coffi.ffi/defcfn
  set-trace-log-level
  "[logLevel] -> void"
  SetTraceLogLevel
  [:coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  mem-realloc
  "[ptr size] -> pointer"
  MemRealloc
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/pointer)
(coffi.ffi/defcfn
  get-frame-time
  "[] -> float"
  GetFrameTime
  []
  :coffi.mem/float)
(coffi.ffi/defcfn
  take-screenshot
  "[fileName] -> void"
  TakeScreenshot
  [:coffi.mem/c-string]
  :coffi.mem/void)
(coffi.ffi/defcfn
  mem-alloc
  "[size] -> pointer"
  MemAlloc
  [:coffi.mem/int]
  :coffi.mem/pointer)
(coffi.ffi/defcfn get-time "[] -> double" GetTime [] :coffi.mem/double)


(coffi.ffi/defcfn
  change-directory
  "[dir] -> bool"
  ChangeDirectory
  [:coffi.mem/c-string]
  ::bool)
(coffi.ffi/defcfn
  get-directory-path
  "[filePath] -> string"
  GetDirectoryPath
  [:coffi.mem/c-string]
  :coffi.mem/c-string)
(coffi.ffi/defcfn
  get-prev-directory-path
  "[dirPath] -> string"
  GetPrevDirectoryPath
  [:coffi.mem/c-string]
  :coffi.mem/c-string)
(coffi.ffi/defcfn
  directory-exists?
  "[dirPath] -> bool"
  DirectoryExists
  [:coffi.mem/c-string]
  ::bool)
(coffi.ffi/defcfn
  get-working-directory
  "[] -> string"
  GetWorkingDirectory
  []
  :coffi.mem/c-string)
(coffi.ffi/defcfn
  export-data-as-code
  "[data size fileName] -> bool"
  ExportDataAsCode
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/c-string]
  ::bool)
(coffi.ffi/defcfn
  get-application-directory
  "[] -> string"
  GetApplicationDirectory
  []
  :coffi.mem/c-string)


(coffi.ffi/defcfn
  get-mouse-wheel-move
  "[] -> float"
  GetMouseWheelMove
  []
  :coffi.mem/float)
(coffi.ffi/defcfn
  key-pressed?
  "[key] -> bool"
  IsKeyPressed
  [:coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  get-touch-point-id
  "[index] -> int"
  GetTouchPointId
  [:coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-mouse-position
  "[] -> vec2"
  GetMousePosition
  []
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  gamepad-available?
  "[gamepad] -> bool"
  IsGamepadAvailable
  [:coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  key-up?
  "[key] -> bool"
  IsKeyUp
  [:coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  gamepad-button-released?
  "[gamepad button] -> bool"
  IsGamepadButtonReleased
  [:coffi.mem/int :coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  get-gamepad-name
  "[gamepad] -> string"
  GetGamepadName
  [:coffi.mem/int]
  :coffi.mem/c-string)
(coffi.ffi/defcfn
  key-down?
  "[key] -> bool"
  IsKeyDown
  [:coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  set-gamepad-mappings
  "[mappings] -> int"
  SetGamepadMappings
  [:coffi.mem/c-string]
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-gamepad-axis-movement
  "[gamepad axis] -> float"
  GetGamepadAxisMovement
  [:coffi.mem/int :coffi.mem/int]
  :coffi.mem/float)
(coffi.ffi/defcfn
  set-mouse-position
  "[x y] -> void"
  SetMousePosition
  [:coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-mouse-offset
  "[offsetX offsetY] -> void"
  SetMouseOffset
  [:coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  mouse-button-up?
  "[button] -> bool"
  IsMouseButtonUp
  [:coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  get-gamepad-axis-count
  "[gamepad] -> int"
  GetGamepadAxisCount
  [:coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn get-mouse-y "[] -> int" GetMouseY [] :coffi.mem/int)
(coffi.ffi/defcfn
  get-mouse-delta
  "[] -> vec2"
  GetMouseDelta
  []
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  get-touch-position
  "[index] -> vec2"
  GetTouchPosition
  [:coffi.mem/int]
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  get-char-pressed
  "[] -> int"
  GetCharPressed
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-gamepad-button-pressed
  "[] -> int"
  GetGamepadButtonPressed
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-touch-point-count
  "[] -> int"
  GetTouchPointCount
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  key-released?
  "[key] -> bool"
  IsKeyReleased
  [:coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  gamepad-button-pressed?
  "[gamepad button] -> bool"
  IsGamepadButtonPressed
  [:coffi.mem/int :coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn get-key-pressed "[] -> int" GetKeyPressed [] :coffi.mem/int)
(coffi.ffi/defcfn
  gamepad-button-up?
  "[gamepad button] -> bool"
  IsGamepadButtonUp
  [:coffi.mem/int :coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  mouse-button-released?
  "[button] -> bool"
  IsMouseButtonReleased
  [:coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  set-mouse-scale
  "[scaleX scaleY] -> void"
  SetMouseScale
  [:coffi.mem/float :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn get-touch-x "[] -> int" GetTouchX [] :coffi.mem/int)
(coffi.ffi/defcfn
  gamepad-button-down?
  "[gamepad button] -> bool"
  IsGamepadButtonDown
  [:coffi.mem/int :coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  get-mouse-wheel-move-v
  "[] -> vec2"
  GetMouseWheelMoveV
  []
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn get-touch-y "[] -> int" GetTouchY [] :coffi.mem/int)
(coffi.ffi/defcfn
  set-exit-key
  "[key] -> void"
  SetExitKey
  [:coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-mouse-cursor
  "[cursor] -> void"
  SetMouseCursor
  [:coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  mouse-button-down?
  "[button] -> bool"
  IsMouseButtonDown
  [:coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn get-mouse-x "[] -> int" GetMouseX [] :coffi.mem/int)
(coffi.ffi/defcfn
  mouse-button-pressed?
  "[button] -> bool"
  IsMouseButtonPressed
  [:coffi.mem/int]
  ::bool)


(coffi.ffi/defcfn
  set-gestures-enabled
  "[flags] -> void"
  SetGesturesEnabled
  [:coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gesture-detected?
  "[gesture] -> bool"
  IsGestureDetected
  [:coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  get-gesture-detected
  "[] -> int"
  GetGestureDetected
  []
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-gesture-hold-duration
  "[] -> float"
  GetGestureHoldDuration
  []
  :coffi.mem/float)
(coffi.ffi/defcfn
  get-gesture-drag-vector
  "[] -> vec2"
  GetGestureDragVector
  []
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  get-gesture-drag-angle
  "[] -> float"
  GetGestureDragAngle
  []
  :coffi.mem/float)
(coffi.ffi/defcfn
  get-gesture-pinch-vector
  "[] -> vec2"
  GetGesturePinchVector
  []
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  get-gesture-pinch-angle
  "[] -> float"
  GetGesturePinchAngle
  []
  :coffi.mem/float)

(def- camera-3d-size (mem/size-of ::camera-3d))

(coffi.ffi/defcfn
  update-camera
  "[camera-3d mode] -> camera-3d"
  UpdateCamera
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void
  native-fn [camera-3d mode]
  (with-open [session (mem/stack-session)]
    (let [arr (mem/serialize camera-3d ::camera-3d session)
          ptr (mem/address-of arr)]
      (native-fn ptr mode)
      (mem/deserialize (mem/as-segment ptr camera-3d-size session) ::camera-3d))))

(coffi.ffi/defcfn
  update-camera-pro
  "[camera-3d movement rotation zoom] -> camera-3d"
  UpdateCameraPro
  [:coffi.mem/pointer
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :coffi.mem/float]
  :coffi.mem/void
  native-fn [camera-3d movement rotation zoom]
  (with-open [session (mem/stack-session)]
    (let [arr (mem/serialize camera-3d ::camera-3d session)
          ptr (mem/address-of arr)]
      (native-fn ptr movement rotation zoom)
      (mem/deserialize (mem/as-segment ptr camera-3d-size session) ::camera-3d))))

(coffi.ffi/defcfn
  check-collision-point-triangle
  "[point p1 p2 p3] -> bool"
  CheckCollisionPointTriangle
  [:raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2]
  ::bool)
(coffi.ffi/defcfn
  draw-poly
  "[center sides radius rotation color] -> void"
  DrawPoly
  [:raylib-clj.core/vec2
   :coffi.mem/int
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  check-collision-lines
  "[startPos1 endPos1 startPos2 endPos2] -> collisionPoint"
  CheckCollisionLines
  [:raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :coffi.mem/pointer]
  ::bool
  native-fn [startPos1 endPos1 startPos2 endPos2]
  (with-open [session (mem/stack-session)]
    (let [arr (mem/alloc-instance ::vec2 session)
          ptr (mem/address-of arr)]
      (if (native-fn startPos1 endPos1 startPos2 endPos2 ptr)
        (mem/deserialize arr ::vec2)
        nil))))

(coffi.ffi/defcfn
  set-shapes-texture
  "[texture source] -> void"
  SetShapesTexture
  [:raylib-clj.core/texture :raylib-clj.core/rectangle]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-rectangle-rounded-lines
  "[rec roundness segments lineThick color] -> void"
  DrawRectangleRoundedLines
  [:raylib-clj.core/rectangle
   :coffi.mem/float
   :coffi.mem/int
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-circle-lines
  "[centerX centerY radius color] -> void"
  DrawCircleLines
  [:coffi.mem/int :coffi.mem/int :coffi.mem/float :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-ring
  "[center innerRadius outerRadius startAngle endAngle segments color] -> void"
  DrawRing
  [:raylib-clj.core/vec2
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-circle-gradient
  "[centerX centerY radius color1 color2] -> void"
  DrawCircleGradient
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/float
   :raylib-clj.core/color
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-line-v
  "[startPos endPos color] -> void"
  DrawLineV
  [:raylib-clj.core/vec2 :raylib-clj.core/vec2 :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-line-b-spline
  "[points thick color] -> void"
  DrawLineBSpline
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/float :raylib-clj.core/color]
  :coffi.mem/void
  native-fn [points thick color]
  (with-open [session (mem/stack-session)]
    (let [cnt (count points)
          arr (mem/serialize points [::mem/array ::vec2 cnt] session)
          ptr (mem/address-of arr)]
      (native-fn ptr cnt thick color))))
(coffi.ffi/defcfn
  draw-pixel-v
  "[position color] -> void"
  DrawPixelV
  [:raylib-clj.core/vec2 :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-circle-sector-lines
  "[center radius startAngle endAngle segments color] -> void"
  DrawCircleSectorLines
  [:raylib-clj.core/vec2
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-poly-lines
  "[center sides radius rotation color] -> void"
  DrawPolyLines
  [:raylib-clj.core/vec2
   :coffi.mem/int
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  check-collision-point-circle
  "[point center radius] -> bool"
  CheckCollisionPointCircle
  [:raylib-clj.core/vec2 :raylib-clj.core/vec2 :coffi.mem/float]
  ::bool)
(coffi.ffi/defcfn
  draw-circle-v
  "[center radius color] -> void"
  DrawCircleV
  [:raylib-clj.core/vec2 :coffi.mem/float :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-rectangle-rec
  "[rec color] -> void"
  DrawRectangleRec
  [:raylib-clj.core/rectangle :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-triangle-lines
  "[v1 v2 v3 color] -> void"
  DrawTriangleLines
  [:raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  check-collision-circle-rec
  "[center radius rec] -> bool"
  CheckCollisionCircleRec
  [:raylib-clj.core/vec2 :coffi.mem/float :raylib-clj.core/rectangle]
  ::bool)
(coffi.ffi/defcfn
  draw-triangle-fan
  "[points color] -> void"
  DrawTriangleFan
  [:coffi.mem/pointer :coffi.mem/int :raylib-clj.core/color]
  :coffi.mem/void
  native-fn [points color]
  (with-open [session (mem/stack-session)]
    (let [cnt (count points)
          arr (mem/serialize points [::mem/array ::vec2 cnt] session)
          ptr (mem/address-of arr)]
      (native-fn ptr cnt color))))
(coffi.ffi/defcfn
  check-collision-point-rec
  "[point rec] -> bool"
  CheckCollisionPointRec
  [:raylib-clj.core/vec2 :raylib-clj.core/rectangle]
  ::bool)
(coffi.ffi/defcfn
  draw-ellipse
  "[centerX centerY radiusH radiusV color] -> void"
  DrawEllipse
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-ring-lines
  "[center innerRadius outerRadius startAngle endAngle segments color] -> void"
  DrawRingLines
  [:raylib-clj.core/vec2
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  check-collision-point-poly
  "[point points] -> bool"
  CheckCollisionPointPoly
  [:raylib-clj.core/vec2 [:coffi.mem/pointer :raylib-clj.core/vec2] :coffi.mem/int]
  ::bool
  native-fn [point points]
  (with-open [session (mem/stack-session)]
    (let [cnt (count points)
          arr (mem/serialize points [::mem/array ::vec2 cnt] session)
          ptr (mem/address-of arr)]
      (native-fn point ptr cnt))))
(coffi.ffi/defcfn
  draw-ellipse-lines
  "[centerX centerY radiusH radiusV color] -> void"
  DrawEllipseLines
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-rectangle-gradient-ex
  "[rec col1 col2 col3 col4] -> void"
  DrawRectangleGradientEx
  [:raylib-clj.core/rectangle
   :raylib-clj.core/color
   :raylib-clj.core/color
   :raylib-clj.core/color
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-triangle-strip
  "[points pointCount color] -> void"
  DrawTriangleStrip
  [:coffi.mem/pointer :coffi.mem/int :raylib-clj.core/color]
  :coffi.mem/void
  native-fn [points color]
  (with-open [session (mem/stack-session)]
    (let [cnt (count points)
          arr (mem/serialize points [::mem/array ::vec2 cnt] session)
          ptr (mem/address-of arr)]
      (native-fn ptr cnt color))))
(coffi.ffi/defcfn
  draw-rectangle-lines-ex
  "[rec lineThick color] -> void"
  DrawRectangleLinesEx
  [:raylib-clj.core/rectangle :coffi.mem/float :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  check-collision-point-line
  "[point p1 p2 threshold] -> bool"
  CheckCollisionPointLine
  [:raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :coffi.mem/int]
  ::bool)
(coffi.ffi/defcfn
  draw-line-bezier-cubic
  "[startPos endPos startControlPos endControlPos thick color] -> void"
  DrawLineBezierCubic
  [:raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  check-collision-circles
  "[center1 radius1 center2 radius2] -> bool"
  CheckCollisionCircles
  [:raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/vec2
   :coffi.mem/float]
  ::bool)
(coffi.ffi/defcfn
  draw-line-strip
  "[points color] -> void"
  DrawLineStrip
  [[:coffi.mem/pointer :raylib-clj.core/point] :coffi.mem/int :raylib-clj.core/color]
  :coffi.mem/void
  native-fn [points color]
  (with-open [session (mem/stack-session)]
    (let [cnt (count points)
          arr (mem/serialize points [::mem/array ::vec2 cnt] session)
          ptr (mem/address-of arr)]
      (native-fn ptr cnt color)))
  )
(coffi.ffi/defcfn
  draw-circle
  "[centerX centerY radius color] -> void"
  DrawCircle
  [:coffi.mem/int :coffi.mem/int :coffi.mem/float :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-collision-rec
  "[rec1 rec2] -> rectangle"
  GetCollisionRec
  [:raylib-clj.core/rectangle :raylib-clj.core/rectangle]
  :raylib-clj.core/rectangle)
(coffi.ffi/defcfn
  draw-line-bezier-quad
  "[startPos endPos controlPos thick color] -> void"
  DrawLineBezierQuad
  [:raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  check-collision-recs
  "[rec1 rec2] -> bool"
  CheckCollisionRecs
  [:raylib-clj.core/rectangle :raylib-clj.core/rectangle]
  ::bool)
(coffi.ffi/defcfn
  draw-rectangle-rounded
  "[rec roundness segments color] -> void"
  DrawRectangleRounded
  [:raylib-clj.core/rectangle
   :coffi.mem/float
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-rectangle
  "[posX posY width height color] -> void"
  DrawRectangle
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-line-catmull-rom
  "[points thick color] -> void"
  DrawLineCatmullRom
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/float :raylib-clj.core/color]
  :coffi.mem/void
  native-fn [points thick color]
  (with-open [session (mem/stack-session)]
    (let [cnt (count points)
          arr (mem/serialize points [::mem/array ::vec2 cnt] session)
          ptr (mem/address-of arr)]
      (native-fn ptr cnt thick color))))
(coffi.ffi/defcfn
  draw-line-bezier
  "[startPos endPos thick color] -> void"
  DrawLineBezier
  [:raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-line
  "[startPosX startPosY endPosX endPosY color] -> void"
  DrawLine
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-poly-lines-ex
  "[center sides radius rotation lineThick color] -> void"
  DrawPolyLinesEx
  [:raylib-clj.core/vec2
   :coffi.mem/int
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-pixel
  "[posX posY color] -> void"
  DrawPixel
  [:coffi.mem/int :coffi.mem/int :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-rectangle-lines
  "[posX posY width height color] -> void"
  DrawRectangleLines
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-rectangle-gradient-v
  "[posX posY width height color1 color2] -> void"
  DrawRectangleGradientV
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-circle-sector
  "[center radius startAngle endAngle segments color] -> void"
  DrawCircleSector
  [:raylib-clj.core/vec2
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-triangle
  "[v1 v2 v3 color] -> void"
  DrawTriangle
  [:raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-line-ex
  "[startPos endPos thick color] -> void"
  DrawLineEx
  [:raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-rectangle-pro
  "[rec origin rotation color] -> void"
  DrawRectanglePro
  [:raylib-clj.core/rectangle
   :raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-rectangle-gradient-h
  "[posX posY width height color1 color2] -> void"
  DrawRectangleGradientH
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-rectangle-v
  "[position size color] -> void"
  DrawRectangleV
  [:raylib-clj.core/vec2 :raylib-clj.core/vec2 :raylib-clj.core/color]
  :coffi.mem/void)


(coffi.ffi/defcfn
  image-color-grayscale
  "[image] -> void"
  ImageColorGrayscale
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  export-image
  "[image fileName] -> bool"
  ExportImage
  [:raylib-clj.core/image :coffi.mem/c-string]
  ::bool)
(coffi.ffi/defcfn
  load-image-colors
  "[image] -> string"
  LoadImageColors
  [:raylib-clj.core/image]
  :coffi.mem/c-string)
(coffi.ffi/defcfn
  unload-image
  "[image] -> void"
  UnloadImage
  [:raylib-clj.core/image]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-blur-gaussian
  "[image blurSize] -> void"
  ImageBlurGaussian
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-format
  "[image newFormat] -> void"
  ImageFormat
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-mipmaps
  "[image] -> void"
  ImageMipmaps
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-image-gradient-radial
  "[width height density inner outer] -> image"
  GenImageGradientRadial
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/float
   :raylib-clj.core/color
   :raylib-clj.core/color]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  export-image-as-code
  "[image fileName] -> bool"
  ExportImageAsCode
  [:raylib-clj.core/image :coffi.mem/c-string]
  ::bool)
(coffi.ffi/defcfn
  gen-image-gradient-linear
  "[width height direction start end] -> image"
  GenImageGradientLinear
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color
   :raylib-clj.core/color]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-copy
  "[image] -> image"
  ImageCopy
  [:raylib-clj.core/image]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-resize
  "[image newWidth newHeight] -> void"
  ImageResize
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-ready?
  "[image] -> bool"
  IsImageReady
  [:raylib-clj.core/image]
  ::bool)
(coffi.ffi/defcfn
  image-dither
  "[image rBpp gBpp bBpp aBpp] -> void"
  ImageDither
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-image-palette
  "[image maxPaletteSize colorCount] -> pointer"
  LoadImagePalette
  [:raylib-clj.core/image :coffi.mem/int :coffi.mem/pointer]
  :coffi.mem/pointer)
(coffi.ffi/defcfn
  image-to-pot
  "[image fill] -> void"
  ImageToPOT
  [:coffi.mem/pointer :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-from-image
  "[image rec] -> image"
  ImageFromImage
  [:raylib-clj.core/image :raylib-clj.core/rectangle]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-resize-canvas
  "[image newWidth newHeight offsetX offsetY fill] -> void"
  ImageResizeCanvas
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-image-from-memory
  "[fileType fileData dataSize] -> image"
  LoadImageFromMemory
  [:coffi.mem/c-string :coffi.mem/pointer :coffi.mem/int]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  get-image-color
  "[image x y] -> color"
  GetImageColor
  [:raylib-clj.core/image :coffi.mem/int :coffi.mem/int]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  gen-image-cellular
  "[width height tileSize] -> image"
  GenImageCellular
  [:coffi.mem/int :coffi.mem/int :coffi.mem/int]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  unload-image-palette
  "[colors] -> void"
  UnloadImagePalette
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-rotate
  "[image degrees] -> void"
  ImageRotate
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-text-ex
  "[font text fontSize spacing tint] -> image"
  ImageTextEx
  [:raylib-clj.core/font
   :coffi.mem/c-string
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-flip-vertical
  "[image] -> void"
  ImageFlipVertical
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-color-tint
  "[image color] -> void"
  ImageColorTint
  [:coffi.mem/pointer :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-alpha-mask
  "[image alphaMask] -> void"
  ImageAlphaMask
  [:coffi.mem/pointer :raylib-clj.core/image]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-image-text
  "[width height text] -> image"
  GenImageText
  [:coffi.mem/int :coffi.mem/int :coffi.mem/c-string]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  load-image-anim
  "[fileName frames] -> image"
  LoadImageAnim
  [:coffi.mem/c-string :coffi.mem/pointer]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  load-image-raw
  "[fileName width height format headerSize] -> image"
  LoadImageRaw
  [:coffi.mem/c-string
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  export-image-to-memory
  "[image fileType fileSize] -> pointer"
  ExportImageToMemory
  [:raylib-clj.core/image :coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/pointer)
(coffi.ffi/defcfn
  image-color-replace
  "[image color replace] -> void"
  ImageColorReplace
  [:coffi.mem/pointer :raylib-clj.core/color :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-crop
  "[image crop] -> void"
  ImageCrop
  [:coffi.mem/pointer :raylib-clj.core/rectangle]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-image-alpha-border
  "[image threshold] -> rectangle"
  GetImageAlphaBorder
  [:raylib-clj.core/image :coffi.mem/float]
  :raylib-clj.core/rectangle)
(coffi.ffi/defcfn
  unload-image-colors
  "[colors] -> void"
  UnloadImageColors
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-image-white-noise
  "[width height factor] -> image"
  GenImageWhiteNoise
  [:coffi.mem/int :coffi.mem/int :coffi.mem/float]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-color-brightness
  "[image brightness] -> void"
  ImageColorBrightness
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-rotate-cw
  "[image] -> void"
  ImageRotateCW
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-image-from-texture
  "[texture] -> image"
  LoadImageFromTexture
  [:raylib-clj.core/texture]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-resize-nn
  "[image newWidth newHeight] -> void"
  ImageResizeNN
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-image-color
  "[width height color] -> image"
  GenImageColor
  [:coffi.mem/int :coffi.mem/int :raylib-clj.core/color]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-rotate-ccw
  "[image] -> void"
  ImageRotateCCW
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-image-gradient-square
  "[width height density inner outer] -> image"
  GenImageGradientSquare
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/float
   :raylib-clj.core/color
   :raylib-clj.core/color]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-color-contrast
  "[image contrast] -> void"
  ImageColorContrast
  [:coffi.mem/pointer :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-image-checked
  "[width height checksX checksY col1 col2] -> image"
  GenImageChecked
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color
   :raylib-clj.core/color]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-color-invert
  "[image] -> void"
  ImageColorInvert
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-alpha-premultiply
  "[image] -> void"
  ImageAlphaPremultiply
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-image-perlin-noise
  "[width height offsetX offsetY scale] -> image"
  GenImagePerlinNoise
  [:coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/float]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-alpha-crop
  "[image threshold] -> void"
  ImageAlphaCrop
  [:coffi.mem/pointer :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-flip-horizontal
  "[image] -> void"
  ImageFlipHorizontal
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-image-from-screen
  "[] -> image"
  LoadImageFromScreen
  []
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  image-alpha-clear
  "[image color threshold] -> void"
  ImageAlphaClear
  [:coffi.mem/pointer :raylib-clj.core/color :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-text
  "[text fontSize color] -> image"
  ImageText
  [:coffi.mem/c-string :coffi.mem/int :raylib-clj.core/color]
  :raylib-clj.core/image)
(coffi.ffi/defcfn
  load-image
  "[fileName] -> image"
  LoadImage
  [:coffi.mem/pointer]
  :raylib-clj.core/image)


(coffi.ffi/defcfn
  image-draw-pixel-v
  "[image position color] -> void"
  ImageDrawPixelV
  [:coffi.mem/pointer :raylib-clj.core/vec2 :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-circle
  "[image centerX centerY radius color] -> void"
  ImageDrawCircle
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-circle-lines
  "[image centerX centerY radius color] -> void"
  ImageDrawCircleLines
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-text-ex
  "[image font text position fontSize spacing tint] -> void"
  ImageDrawTextEx
  [:coffi.mem/pointer
   :raylib-clj.core/font
   :coffi.mem/c-string
   :raylib-clj.core/vec2
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-line-v
  "[image start end color] -> void"
  ImageDrawLineV
  [:coffi.mem/pointer
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-circle-v
  "[image center radius color] -> void"
  ImageDrawCircleV
  [:coffi.mem/pointer
   :raylib-clj.core/vec2
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-pixel
  "[image posX posY color] -> void"
  ImageDrawPixel
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-line
  "[image startPosX startPosY endPosX endPosY color] -> void"
  ImageDrawLine
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw
  "[image src srcRec dstRec tint] -> void"
  ImageDraw
  [:coffi.mem/pointer
   :raylib-clj.core/image
   :raylib-clj.core/rectangle
   :raylib-clj.core/rectangle
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-clear-background
  "[image color] -> void"
  ImageClearBackground
  [:coffi.mem/pointer :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-rectangle-v
  "[image position size color] -> void"
  ImageDrawRectangleV
  [:coffi.mem/pointer
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-rectangle-rec
  "[image rec color] -> void"
  ImageDrawRectangleRec
  [:coffi.mem/pointer :raylib-clj.core/rectangle :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-circle-lines-v
  "[image center radius color] -> void"
  ImageDrawCircleLinesV
  [:coffi.mem/pointer
   :raylib-clj.core/vec2
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-rectangle-lines
  "[image rec thick color] -> void"
  ImageDrawRectangleLines
  [:coffi.mem/pointer
   :raylib-clj.core/rectangle
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-text
  "[image text posX posY fontSize color] -> void"
  ImageDrawText
  [:coffi.mem/pointer
   :coffi.mem/c-string
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  image-draw-rectangle
  "[image posX posY width height color] -> void"
  ImageDrawRectangle
  [:coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)



(coffi.ffi/defcfn
  load-texture-cubemap
  "[image layout] -> texture"
  LoadTextureCubemap
  [:raylib-clj.core/image :coffi.mem/int]
  :raylib-clj.core/texture)
(coffi.ffi/defcfn
  color-alpha
  "[color alpha] -> color"
  ColorAlpha
  [:raylib-clj.core/color :coffi.mem/float]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  update-texture-rec
  "[texture rec pixels] -> void"
  UpdateTextureRec
  [:raylib-clj.core/texture :raylib-clj.core/rectangle :coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-texture-pro
  "[texture source dest origin rotation tint] -> void"
  DrawTexturePro
  [:raylib-clj.core/texture
   :raylib-clj.core/rectangle
   :raylib-clj.core/rectangle
   :raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  color-to-hsv
  "[color] -> vec3"
  ColorToHSV
  [:raylib-clj.core/color]
  :raylib-clj.core/vec3)
(coffi.ffi/defcfn
  color-normalize
  "[color] -> vec4"
  ColorNormalize
  [:raylib-clj.core/color]
  :raylib-clj.core/vec4)
(coffi.ffi/defcfn
  color-brightness
  "[color factor] -> color"
  ColorBrightness
  [:raylib-clj.core/color :coffi.mem/float]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  color-contrast
  "[color contrast] -> color"
  ColorContrast
  [:raylib-clj.core/color :coffi.mem/float]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  load-render-texture
  "[width height] -> render-texture"
  LoadRenderTexture
  [:coffi.mem/int :coffi.mem/int]
  :raylib-clj.core/render-texture)
(coffi.ffi/defcfn
  unload-render-texture
  "[target] -> void"
  UnloadRenderTexture
  [:raylib-clj.core/render-texture]
  :coffi.mem/void)
(coffi.ffi/defcfn
  color-to-int
  "[color] -> int"
  ColorToInt
  [:raylib-clj.core/color]
  :coffi.mem/int)
(coffi.ffi/defcfn
  draw-texture-ex
  "[texture position rotation scale tint] -> void"
  DrawTextureEx
  [:raylib-clj.core/texture
   :raylib-clj.core/vec2
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  fade
  "[color alpha] -> color"
  Fade
  [:raylib-clj.core/color :coffi.mem/float]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  set-texture-wrap
  "[texture wrap] -> void"
  SetTextureWrap
  [:raylib-clj.core/texture :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-pixel-color
  "[dstPtr color format] -> void"
  SetPixelColor
  [:coffi.mem/pointer :raylib-clj.core/color :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-texture-filter
  "[texture filter] -> void"
  SetTextureFilter
  [:raylib-clj.core/texture :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-texture-from-image
  "[image] -> texture"
  LoadTextureFromImage
  [:raylib-clj.core/image]
  :raylib-clj.core/texture)
(coffi.ffi/defcfn
  color-tint
  "[color tint] -> color"
  ColorTint
  [:raylib-clj.core/color :raylib-clj.core/color]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  update-texture
  "[texture pixels] -> void"
  UpdateTexture
  [:raylib-clj.core/texture :coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-texture-rec
  "[texture source position tint] -> void"
  DrawTextureRec
  [:raylib-clj.core/texture
   :raylib-clj.core/rectangle
   :raylib-clj.core/vec2
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-texture-mipmaps
  "[texture] -> void"
  GenTextureMipmaps
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-color
  "[hexValue] -> color"
  GetColor
  [:coffi.mem/int]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  draw-texture-v
  "[texture position tint] -> void"
  DrawTextureV
  [:raylib-clj.core/texture :raylib-clj.core/vec2 :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  color-from-normalized
  "[normalized] -> color"
  ColorFromNormalized
  [:raylib-clj.core/vec4]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  color-from-hsv
  "[hue saturation value] -> color"
  ColorFromHSV
  [:coffi.mem/float :coffi.mem/float :coffi.mem/float]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  render-texture-ready?
  "[target] -> bool"
  IsRenderTextureReady
  [:raylib-clj.core/render-texture]
  ::bool)
(coffi.ffi/defcfn
  load-texture
  "[fileName] -> texture"
  LoadTexture
  [:coffi.mem/c-string]
  :raylib-clj.core/texture)
(coffi.ffi/defcfn
  get-pixel-color
  "[srcPtr format] -> color"
  GetPixelColor
  [:coffi.mem/pointer :coffi.mem/int]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  draw-texture
  "[texture posX posY tint] -> void"
  DrawTexture
  [:raylib-clj.core/texture
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-texture-n-patch
  "[texture nPatchInfo dest origin rotation tint] -> void"
  DrawTextureNPatch
  [:raylib-clj.core/texture
   :raylib-clj.core/n-patch-info
   :raylib-clj.core/rectangle
   :raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  unload-texture
  "[texture] -> void"
  UnloadTexture
  [:raylib-clj.core/texture]
  :coffi.mem/void)
(coffi.ffi/defcfn
  color-alpha-blend
  "[dst src tint] -> color"
  ColorAlphaBlend
  [:raylib-clj.core/color :raylib-clj.core/color :raylib-clj.core/color]
  :raylib-clj.core/color)
(coffi.ffi/defcfn
  get-pixel-data-size
  "[width height format] -> int"
  GetPixelDataSize
  [:coffi.mem/int :coffi.mem/int :coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn
  texture-ready?
  "[texture] -> bool"
  IsTextureReady
  [:raylib-clj.core/texture]
  ::bool)



(coffi.ffi/defcfn
  load-utf-8
  "[codepoints length] -> pointer"
  LoadUTF8
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/pointer)
(coffi.ffi/defcfn
  get-codepoint-next
  "[text codepointSize] -> int"
  GetCodepointNext
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-glyph-info
  "[font codepoint] -> glyph-info"
  GetGlyphInfo
  [:raylib-clj.core/font :coffi.mem/int]
  :raylib-clj.core/glyph-info)
(coffi.ffi/defcfn
  load-codepoints
  "[text count] -> pointer"
  LoadCodepoints
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/pointer)
(coffi.ffi/defcfn
  get-glyph-atlas-rec
  "[font codepoint] -> rectangle"
  GetGlyphAtlasRec
  [:raylib-clj.core/font :coffi.mem/int]
  :raylib-clj.core/rectangle)
(coffi.ffi/defcfn
  get-codepoint
  "[text codepointSize] -> int"
  GetCodepoint
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/int)
(coffi.ffi/defcfn
  measure-text
  "[text fontSize] -> int"
  MeasureText
  [:coffi.mem/c-string :coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn
  get-glyph-index
  "[font codepoint] -> int"
  GetGlyphIndex
  [:raylib-clj.core/font :coffi.mem/int]
  :coffi.mem/int)
(coffi.ffi/defcfn
  measure-text-ex
  "[font text fontSize spacing] -> vec2"
  MeasureTextEx
  [:raylib-clj.core/font :coffi.mem/c-string :coffi.mem/float :coffi.mem/float]
  :raylib-clj.core/vec2)
(coffi.ffi/defcfn
  unload-utf-8
  "[text] -> void"
  UnloadUTF8
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-codepoint-count
  "[text] -> int"
  GetCodepointCount
  [:coffi.mem/c-string]
  :coffi.mem/int)
(coffi.ffi/defcfn
  set-text-line-spacing
  "[spacing] -> void"
  SetTextLineSpacing
  [:coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-codepoint-previous
  "[text codepointSize] -> int"
  GetCodepointPrevious
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/int)
(coffi.ffi/defcfn
  codepoint-to-utf-8
  "[codepoint utf8Size] -> string"
  CodepointToUTF8
  [:coffi.mem/int :coffi.mem/pointer]
  :coffi.mem/c-string)
(coffi.ffi/defcfn
  unload-codepoints
  "[codepoints] -> void"
  UnloadCodepoints
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  text-to-integer
  "[text] -> int"
  TextToInteger
  [:coffi.mem/c-string]
  :coffi.mem/int)


(coffi.ffi/defcfn
  draw-circle-3-d
  "[center radius rotationAxis rotationAngle color] -> void"
  DrawCircle3D
  [:raylib-clj.core/vec3
   :coffi.mem/float
   :raylib-clj.core/vec3
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-cylinder-ex
  "[startPos endPos startRadius endRadius sides color] -> void"
  DrawCylinderEx
  [:raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-cube-wires-v
  "[position size color] -> void"
  DrawCubeWiresV
  [:raylib-clj.core/vec3 :raylib-clj.core/vec3 :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-capsule-wires
  "[startPos endPos radius slices rings color] -> void"
  DrawCapsuleWires
  [:raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-ray
  "[ray color] -> void"
  DrawRay
  [:raylib-clj.core/ray :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-cylinder-wires
  "[position radiusTop radiusBottom height slices color] -> void"
  DrawCylinderWires
  [:raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-sphere-wires
  "[centerPos radius rings slices color] -> void"
  DrawSphereWires
  [:raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-triangle-strip-3-d
  "[points color] -> void"
  DrawTriangleStrip3D
  [:coffi.mem/pointer :coffi.mem/int :raylib-clj.core/color]
  :coffi.mem/void
  native-fn [points color]
  (with-open [session (mem/stack-session)]
    (let [cnt (count points)
          arr (mem/serialize points [::mem/array ::vec2 cnt] session)
          ptr (mem/address-of arr)]
      (native-fn ptr cnt color))))
(coffi.ffi/defcfn
  draw-plane
  "[centerPos size color] -> void"
  DrawPlane
  [:raylib-clj.core/vec3 :raylib-clj.core/vec2 :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-line-3-d
  "[startPos endPos color] -> void"
  DrawLine3D
  [:raylib-clj.core/vec3 :raylib-clj.core/vec3 :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-cube-wires
  "[position width height length color] -> void"
  DrawCubeWires
  [:raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-cube-v
  "[position size color] -> void"
  DrawCubeV
  [:raylib-clj.core/vec3 :raylib-clj.core/vec3 :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-cube
  "[position width height length color] -> void"
  DrawCube
  [:raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-point-3-d
  "[position color] -> void"
  DrawPoint3D
  [:raylib-clj.core/vec3 :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-triangle-3-d
  "[v1 v2 v3 color] -> void"
  DrawTriangle3D
  [:raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-sphere
  "[centerPos radius color] -> void"
  DrawSphere
  [:raylib-clj.core/vec3 :coffi.mem/float :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-cylinder-wires-ex
  "[startPos endPos startRadius endRadius sides color] -> void"
  DrawCylinderWiresEx
  [:raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-capsule
  "[startPos endPos radius slices rings color] -> void"
  DrawCapsule
  [:raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-sphere-ex
  "[centerPos radius rings slices color] -> void"
  DrawSphereEx
  [:raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/int
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-grid
  "[slices spacing] -> void"
  DrawGrid
  [:coffi.mem/int :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-cylinder
  "[position radiusTop radiusBottom height slices color] -> void"
  DrawCylinder
  [:raylib-clj.core/vec3
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/float
   :coffi.mem/int
   :raylib-clj.core/color]
  :coffi.mem/void)



(coffi.ffi/defcfn
  gen-mesh-cone
  "[radius height slices] -> mesh"
  GenMeshCone
  [:coffi.mem/float :coffi.mem/float :coffi.mem/int]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  gen-mesh-knot
  "[radius size radSeg sides] -> mesh"
  GenMeshKnot
  [:coffi.mem/float :coffi.mem/float :coffi.mem/int :coffi.mem/int]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  unload-model
  "[model] -> void"
  UnloadModel
  [:raylib-clj.core/model]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-billboard
  "[camera texture position size tint] -> void"
  DrawBillboard
  [:raylib-clj.core/camera-3d
   :raylib-clj.core/texture
   :raylib-clj.core/vec3
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-model-ex
  "[model position rotationAxis rotationAngle scale tint] -> void"
  DrawModelEx
  [:raylib-clj.core/model
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :coffi.mem/float
   :raylib-clj.core/vec3
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-model
  "[fileName] -> model"
  LoadModel
  [:coffi.mem/c-string]
  :raylib-clj.core/model)
(coffi.ffi/defcfn
  draw-billboard-rec
  "[camera texture source position size tint] -> void"
  DrawBillboardRec
  [:raylib-clj.core/camera-3d
   :raylib-clj.core/texture
   :raylib-clj.core/rectangle
   :raylib-clj.core/vec3
   :raylib-clj.core/vec2
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-mesh-poly
  "[sides radius] -> mesh"
  GenMeshPoly
  [:coffi.mem/int :coffi.mem/float]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  gen-mesh-cylinder
  "[radius height slices] -> mesh"
  GenMeshCylinder
  [:coffi.mem/float :coffi.mem/float :coffi.mem/int]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  export-mesh
  "[mesh fileName] -> bool"
  ExportMesh
  [:raylib-clj.core/mesh :coffi.mem/c-string]
  ::bool)
(coffi.ffi/defcfn
  gen-mesh-hemi-sphere
  "[radius rings slices] -> mesh"
  GenMeshHemiSphere
  [:coffi.mem/float :coffi.mem/int :coffi.mem/int]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  gen-mesh-cube
  "[width height length] -> mesh"
  GenMeshCube
  [:coffi.mem/float :coffi.mem/float :coffi.mem/float]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  draw-model
  "[model position scale tint] -> void"
  DrawModel
  [:raylib-clj.core/model
   :raylib-clj.core/vec3
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-mesh-tangents
  "[mesh] -> void"
  GenMeshTangents
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  unload-mesh
  "[mesh] -> void"
  UnloadMesh
  [:raylib-clj.core/mesh]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-mesh-instanced
  "[mesh material transforms instances] -> void"
  DrawMeshInstanced
  [:raylib-clj.core/mesh
   :raylib-clj.core/material
   :coffi.mem/pointer
   :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  update-mesh-buffer
  "[mesh index data dataSize offset] -> void"
  UpdateMeshBuffer
  [:raylib-clj.core/mesh
   :coffi.mem/int
   :coffi.mem/pointer
   :coffi.mem/int
   :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-model-bounding-box
  "[model] -> bounding-box"
  GetModelBoundingBox
  [:raylib-clj.core/model]
  :raylib-clj.core/bounding-box)
(coffi.ffi/defcfn
  upload-mesh
  "[mesh dynamic] -> void"
  UploadMesh
  [:coffi.mem/pointer ::bool]
  :coffi.mem/void)
(coffi.ffi/defcfn
  model-ready?
  "[model] -> bool"
  IsModelReady
  [:raylib-clj.core/model]
  ::bool)
(coffi.ffi/defcfn
  gen-mesh-cubicmap
  "[cubicmap cubeSize] -> mesh"
  GenMeshCubicmap
  [:raylib-clj.core/image :raylib-clj.core/vec3]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  load-model-from-mesh
  "[mesh] -> model"
  LoadModelFromMesh
  [:raylib-clj.core/mesh]
  :raylib-clj.core/model)
(coffi.ffi/defcfn
  draw-mesh
  "[mesh material transform] -> void"
  DrawMesh
  [:raylib-clj.core/mesh :raylib-clj.core/material :raylib-clj.core/mat4]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-mesh-plane
  "[width length resX resZ] -> mesh"
  GenMeshPlane
  [:coffi.mem/float :coffi.mem/float :coffi.mem/int :coffi.mem/int]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  gen-mesh-sphere
  "[radius rings slices] -> mesh"
  GenMeshSphere
  [:coffi.mem/float :coffi.mem/int :coffi.mem/int]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  draw-model-wires
  "[model position scale tint] -> void"
  DrawModelWires
  [:raylib-clj.core/model
   :raylib-clj.core/vec3
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-model-wires-ex
  "[model position rotationAxis rotationAngle scale tint] -> void"
  DrawModelWiresEx
  [:raylib-clj.core/model
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :coffi.mem/float
   :raylib-clj.core/vec3
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  gen-mesh-heightmap
  "[heightmap size] -> mesh"
  GenMeshHeightmap
  [:raylib-clj.core/image :raylib-clj.core/vec3]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  get-mesh-bounding-box
  "[mesh] -> bounding-box"
  GetMeshBoundingBox
  [:raylib-clj.core/mesh]
  :raylib-clj.core/bounding-box)
(coffi.ffi/defcfn
  gen-mesh-torus
  "[radius size radSeg sides] -> mesh"
  GenMeshTorus
  [:coffi.mem/float :coffi.mem/float :coffi.mem/int :coffi.mem/int]
  :raylib-clj.core/mesh)
(coffi.ffi/defcfn
  draw-billboard-pro
  "[camera texture source position up size origin rotation tint] -> void"
  DrawBillboardPro
  [:raylib-clj.core/camera-3d
   :raylib-clj.core/texture
   :raylib-clj.core/rectangle
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :raylib-clj.core/vec2
   :raylib-clj.core/vec2
   :coffi.mem/float
   :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  draw-bounding-box
  "[box color] -> void"
  DrawBoundingBox
  [:raylib-clj.core/bounding-box :raylib-clj.core/color]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-ray-collision-triangle
  "[ray p1 p2 p3] -> ray-collision"
  GetRayCollisionTriangle
  [:raylib-clj.core/ray
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3]
  :raylib-clj.core/ray-collision)
(coffi.ffi/defcfn
  material-ready?
  "[material] -> bool"
  IsMaterialReady
  [:raylib-clj.core/material]
  ::bool)
(coffi.ffi/defcfn
  update-model-animation
  "[model anim frame] -> void"
  UpdateModelAnimation
  [:raylib-clj.core/model :raylib-clj.core/model-animation :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  unload-material
  "[material] -> void"
  UnloadMaterial
  [:raylib-clj.core/material]
  :coffi.mem/void)
(coffi.ffi/defcfn
  check-collision-spheres
  "[center1 radius1 center2 radius2] -> bool"
  CheckCollisionSpheres
  [:raylib-clj.core/vec3
   :coffi.mem/float
   :raylib-clj.core/vec3
   :coffi.mem/float]
  ::bool)
(coffi.ffi/defcfn
  model-animation-valid?
  "[model anim] -> bool"
  IsModelAnimationValid
  [:raylib-clj.core/model :raylib-clj.core/model-animation]
  ::bool)
(coffi.ffi/defcfn
  get-ray-collision-sphere
  "[ray center radius] -> ray-collision"
  GetRayCollisionSphere
  [:raylib-clj.core/ray :raylib-clj.core/vec3 :coffi.mem/float]
  :raylib-clj.core/ray-collision)
(coffi.ffi/defcfn
  check-collision-box-sphere
  "[box center radius] -> bool"
  CheckCollisionBoxSphere
  [:raylib-clj.core/bounding-box :raylib-clj.core/vec3 :coffi.mem/float]
  ::bool)
(coffi.ffi/defcfn
  unload-model-animations
  "[animations count] -> void"
  UnloadModelAnimations
  [:coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-ray-collision-mesh
  "[ray mesh transform] -> ray-collision"
  GetRayCollisionMesh
  [:raylib-clj.core/ray :raylib-clj.core/mesh :raylib-clj.core/mat4]
  :raylib-clj.core/ray-collision)
(coffi.ffi/defcfn
  set-material-texture
  "[material mapType texture] -> void"
  SetMaterialTexture
  [:coffi.mem/pointer :coffi.mem/int :raylib-clj.core/texture]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-ray-collision-quad
  "[ray p1 p2 p3 p4] -> ray-collision"
  GetRayCollisionQuad
  [:raylib-clj.core/ray
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3
   :raylib-clj.core/vec3]
  :raylib-clj.core/ray-collision)
(coffi.ffi/defcfn
  get-ray-collision-box
  "[ray box] -> ray-collision"
  GetRayCollisionBox
  [:raylib-clj.core/ray :raylib-clj.core/bounding-box]
  :raylib-clj.core/ray-collision)
(coffi.ffi/defcfn
  set-model-mesh-material
  "[model meshId materialId] -> void"
  SetModelMeshMaterial
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-model-animations
  "[fileName animCount] -> pointer"
  LoadModelAnimations
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/pointer)
(coffi.ffi/defcfn
  load-material-default
  "[] -> material"
  LoadMaterialDefault
  []
  :raylib-clj.core/material)
(coffi.ffi/defcfn
  load-materials
  "[fileName materialCount] -> pointer"
  LoadMaterials
  [:coffi.mem/c-string :coffi.mem/pointer]
  :coffi.mem/pointer)
(coffi.ffi/defcfn
  check-collision-boxes
  "[box1 box2] -> bool"
  CheckCollisionBoxes
  [:raylib-clj.core/bounding-box :raylib-clj.core/bounding-box]
  ::bool)
(coffi.ffi/defcfn
  unload-model-animation
  "[anim] -> void"
  UnloadModelAnimation
  [:raylib-clj.core/model-animation]
  :coffi.mem/void)



(coffi.ffi/defcfn
  load-music-stream
  "[fileName] -> music"
  LoadMusicStream
  [:coffi.mem/c-string]
  :raylib-clj.core/music)
(coffi.ffi/defcfn
  load-music-stream-from-memory
  "[fileType data dataSize] -> music"
  LoadMusicStreamFromMemory
  [:coffi.mem/c-string :coffi.mem/pointer :coffi.mem/int]
  :raylib-clj.core/music)
(coffi.ffi/defcfn
  update-sound
  "[sound data sampleCount] -> void"
  UpdateSound
  [:raylib-clj.core/sound :coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  unload-music-stream
  "[music] -> void"
  UnloadMusicStream
  [:raylib-clj.core/music]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-wave
  "[fileName] -> wave"
  LoadWave
  [:coffi.mem/c-string]
  :raylib-clj.core/wave)
(coffi.ffi/defcfn
  export-wave
  "[wave fileName] -> bool"
  ExportWave
  [:raylib-clj.core/wave :coffi.mem/c-string]
  ::bool)
(coffi.ffi/defcfn
  set-audio-stream-pan
  "[stream pan] -> void"
  SetAudioStreamPan
  [:raylib-clj.core/audio-stream :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  stop-sound
  "[sound] -> void"
  StopSound
  [:raylib-clj.core/sound]
  :coffi.mem/void)
(coffi.ffi/defcfn
  export-wave-as-code
  "[wave fileName] -> bool"
  ExportWaveAsCode
  [:raylib-clj.core/wave :coffi.mem/c-string]
  ::bool)
(coffi.ffi/defcfn
  stop-music-stream
  "[music] -> void"
  StopMusicStream
  [:raylib-clj.core/music]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-audio-stream
  "[sampleRate sampleSize channels] -> audio-stream"
  LoadAudioStream
  [:coffi.mem/int :coffi.mem/int :coffi.mem/int]
  :raylib-clj.core/audio-stream)
(coffi.ffi/defcfn
  set-audio-stream-buffer-size-default
  "[size] -> void"
  SetAudioStreamBufferSizeDefault
  [:coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-sound-from-wave
  "[wave] -> sound"
  LoadSoundFromWave
  [:raylib-clj.core/wave]
  :raylib-clj.core/sound)
(coffi.ffi/defcfn
  set-music-pitch
  "[music pitch] -> void"
  SetMusicPitch
  [:raylib-clj.core/music :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  get-music-time-length
  "[music] -> float"
  GetMusicTimeLength
  [:raylib-clj.core/music]
  :coffi.mem/float)
(coffi.ffi/defcfn
  resume-music-stream
  "[music] -> void"
  ResumeMusicStream
  [:raylib-clj.core/music]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-music-volume
  "[music volume] -> void"
  SetMusicVolume
  [:raylib-clj.core/music :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  sound-playing?
  "[sound] -> bool"
  IsSoundPlaying
  [:raylib-clj.core/sound]
  ::bool)
(coffi.ffi/defcfn
  sound-ready?
  "[sound] -> bool"
  IsSoundReady
  [:raylib-clj.core/sound]
  ::bool)
(coffi.ffi/defcfn
  update-music-stream
  "[music] -> void"
  UpdateMusicStream
  [:raylib-clj.core/music]
  :coffi.mem/void)
(coffi.ffi/defcfn
  play-music-stream
  "[music] -> void"
  PlayMusicStream
  [:raylib-clj.core/music]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-sound-pan
  "[sound pan] -> void"
  SetSoundPan
  [:raylib-clj.core/sound :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  music-ready?
  "[music] -> bool"
  IsMusicReady
  [:raylib-clj.core/music]
  ::bool)
(coffi.ffi/defcfn
  update-audio-stream
  "[stream data frameCount] -> void"
  UpdateAudioStream
  [:raylib-clj.core/audio-stream :coffi.mem/pointer :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  play-sound
  "[sound] -> void"
  PlaySound
  [:raylib-clj.core/sound]
  :coffi.mem/void)
(coffi.ffi/defcfn
  resume-sound
  "[sound] -> void"
  ResumeSound
  [:raylib-clj.core/sound]
  :coffi.mem/void)
(coffi.ffi/defcfn
  audio-stream-ready?
  "[stream] -> bool"
  IsAudioStreamReady
  [:raylib-clj.core/audio-stream]
  ::bool)
(coffi.ffi/defcfn
  load-sound
  "[fileName] -> sound"
  LoadSound
  [:coffi.mem/c-string]
  :raylib-clj.core/sound)
(coffi.ffi/defcfn
  music-stream-playing?
  "[music] -> bool"
  IsMusicStreamPlaying
  [:raylib-clj.core/music]
  ::bool)
(coffi.ffi/defcfn
  seek-music-stream
  "[music position] -> void"
  SeekMusicStream
  [:raylib-clj.core/music :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  audio-stream-playing?
  "[stream] -> bool"
  IsAudioStreamPlaying
  [:raylib-clj.core/audio-stream]
  ::bool)
(coffi.ffi/defcfn
  load-wave-from-memory
  "[fileType fileData dataSize] -> wave"
  LoadWaveFromMemory
  [:coffi.mem/c-string :coffi.mem/pointer :coffi.mem/int]
  :raylib-clj.core/wave)
(coffi.ffi/defcfn
  unload-sound
  "[sound] -> void"
  UnloadSound
  [:raylib-clj.core/sound]
  :coffi.mem/void)
(coffi.ffi/defcfn
  pause-music-stream
  "[music] -> void"
  PauseMusicStream
  [:raylib-clj.core/music]
  :coffi.mem/void)
(coffi.ffi/defcfn
  pause-audio-stream
  "[stream] -> void"
  PauseAudioStream
  [:raylib-clj.core/audio-stream]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-audio-stream-pitch
  "[stream pitch] -> void"
  SetAudioStreamPitch
  [:raylib-clj.core/audio-stream :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  stop-audio-stream
  "[stream] -> void"
  StopAudioStream
  [:raylib-clj.core/audio-stream]
  :coffi.mem/void)
(coffi.ffi/defcfn
  wave-copy
  "[wave] -> wave"
  WaveCopy
  [:raylib-clj.core/wave]
  :raylib-clj.core/wave)
(coffi.ffi/defcfn
  set-audio-stream-volume
  "[stream volume] -> void"
  SetAudioStreamVolume
  [:raylib-clj.core/audio-stream :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  play-audio-stream
  "[stream] -> void"
  PlayAudioStream
  [:raylib-clj.core/audio-stream]
  :coffi.mem/void)
(coffi.ffi/defcfn
  wave-ready?
  "[wave] -> bool"
  IsWaveReady
  [:raylib-clj.core/wave]
  ::bool)
(coffi.ffi/defcfn
  set-sound-volume
  "[sound volume] -> void"
  SetSoundVolume
  [:raylib-clj.core/sound :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  wave-format
  "[wave sampleRate sampleSize channels] -> void"
  WaveFormat
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  unload-wave-samples
  "[samples] -> void"
  UnloadWaveSamples
  [:coffi.mem/pointer]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-sound-alias
  "[source] -> sound"
  LoadSoundAlias
  [:raylib-clj.core/sound]
  :raylib-clj.core/sound)
(coffi.ffi/defcfn
  audio-stream-processed?
  "[stream] -> bool"
  IsAudioStreamProcessed
  [:raylib-clj.core/audio-stream]
  ::bool)
(coffi.ffi/defcfn
  set-sound-pitch
  "[sound pitch] -> void"
  SetSoundPitch
  [:raylib-clj.core/sound :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  pause-sound
  "[sound] -> void"
  PauseSound
  [:raylib-clj.core/sound]
  :coffi.mem/void)
(coffi.ffi/defcfn
  unload-sound-alias
  "[alias] -> void"
  UnloadSoundAlias
  [:raylib-clj.core/sound]
  :coffi.mem/void)
(coffi.ffi/defcfn
  load-wave-samples
  "[wave] -> pointer"
  LoadWaveSamples
  [:raylib-clj.core/wave]
  :coffi.mem/pointer)
(coffi.ffi/defcfn
  get-music-time-played
  "[music] -> float"
  GetMusicTimePlayed
  [:raylib-clj.core/music]
  :coffi.mem/float)
(coffi.ffi/defcfn
  unload-wave
  "[wave] -> void"
  UnloadWave
  [:raylib-clj.core/wave]
  :coffi.mem/void)
(coffi.ffi/defcfn
  wave-crop
  "[wave initSample finalSample] -> void"
  WaveCrop
  [:coffi.mem/pointer :coffi.mem/int :coffi.mem/int]
  :coffi.mem/void)
(coffi.ffi/defcfn
  unload-audio-stream
  "[stream] -> void"
  UnloadAudioStream
  [:raylib-clj.core/audio-stream]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-music-pan
  "[music pan] -> void"
  SetMusicPan
  [:raylib-clj.core/music :coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  audio-device-ready?
  "[] -> bool"
  IsAudioDeviceReady
  []
  ::bool)
(coffi.ffi/defcfn
  resume-audio-stream
  "[stream] -> void"
  ResumeAudioStream
  [:raylib-clj.core/audio-stream]
  :coffi.mem/void)
(coffi.ffi/defcfn
  set-master-volume
  "[volume] -> void"
  SetMasterVolume
  [:coffi.mem/float]
  :coffi.mem/void)
(coffi.ffi/defcfn
  close-audio-device
  "[] -> void"
  CloseAudioDevice
  []
  :coffi.mem/void)
(coffi.ffi/defcfn
  init-audio-device
  "[] -> void"
  InitAudioDevice
  []
  :coffi.mem/void)
(comment

  (def state (atom [(System/nanoTime) [1]]))

  (do

    (init-window 800 450 "raylib-clj [core] example - basic window")
    (clear-window-state FLAG_VSYNC_HINT)
    (set-target-fps 240)
    (clear-window-state FLAG_VSYNC_HINT)
    (while (not (window-should-close?))
      (let [[last-time acc] @state
            newtime (System/nanoTime)
            diff (- newtime last-time)
            newacc (vec (take-last 100 (conj acc diff)))
            average-diff (/ (reduce + newacc) (count newacc))
            average-fps (long (/ 1000000000 average-diff))]
        (reset! state [newtime newacc])
        (begin-drawing)
        (clear-background RAYWHITE)
        (draw-text "Congrats! You created your first raylib window!" 190 200 20 BLACK)
        (draw-text "And you did it from clojure!" (int (+ 190 (rand 5))) 240 20 DARKBLUE)
        (draw-text (str "fps: " average-fps ) 190 380 20 BLACK)
        (end-drawing)
        )
      )
    (close-window)
    )

)


(comment
;TODO: what are those types?
;    :SetTraceLogCallback {:rettype :void :argtypes [TraceLogCallback callback]}
;TODO: what to do here?
;:TextFormat {:rettype :pointer :argtypes [[text :pointer] ...]}
;TODO: how to handle callback?
;:SetAudioStreamCallback {:rettype :void :argtypes [[stream :audio-stream] [callback AudioCallback]]}
;:AttachAudioStreamProcessor {:rettype :void :argtypes [[stream :audio-stream] [processor AudioCallback]]}
;:DetachAudioStreamProcessor {:rettype :void :argtypes [[stream :audio-stream] [processor AudioCallback]]}
;:AttachAudioMixedProcessor {:rettype :void :argtypes [[processor AudioCallback]]}
;:DetachAudioMixedProcessor {:rettype :void :argtypes [[processor AudioCallback]]}
         )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
