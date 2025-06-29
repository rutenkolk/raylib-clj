(ns raylib-clj.core
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.string :as s]
   [clojure.set :as sets]
   [clojure.pprint :as pprint]
   [clojure.java.shell :refer [sh with-sh-dir]]
   [clojure.edn :as edn]
   [coffi.ffi :as ffi]
   [coffi.mem :as mem]
   [coffi.layout :as layout]
   [coffimaker.core :as cm]
   [raylib :as rl]
   [clojure.string :as str]
   )
  (:import
   (clojure.lang
    IDeref IFn IMeta IObj IReference)
   (java.lang.invoke
    MethodHandle
    MethodHandles
    MethodType)
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
   (java.nio ByteOrder))
  )


(defn generate-raylib-header-info []
  (cm/c-header-info
   "../raylib/src/raylib.h"
   {:compile-error-replacements {"RL_MALLOC"  "\n"
                                 "RL_CALLOC"  "\n"
                                 "RL_REALLOC" "\n"
                                 "RL_FREE"    " \n"}}))

(def fns-with-pointer-arguments
  {:SetWindowIcons [:in :images :count]
   :LoadFileData [:out ::cm/return :bytesRead]
   :UnloadFileData [::cm/no-str :data]
   :CompressData [[:in :data :dataSize] [:out :as-array ::cm/return :compDataSize]]
   :DecompressData [[:in :compData :compDataSize] [:out :as-array ::cm/return :dataSize]]
   :EncodeDataBase64 [[:in :data :dataSize] [:out :as-array ::cm/return :outputSize]]
   :DecodeDataBase64 [:out :as-array ::cm/return :outputSize]
   :DrawLineBSpline [:points :pointCount]
   :DrawLineCatmullRom [:points :pointCount]
   :DrawLineStrip [:points :pointCount]
   :DrawTriangleFan [:points :pointCount]
   :DrawTriangleStrip [:points :pointCount]
   :CheckCollisionPointPoly [:points :pointCount]
   :CheckCollisionLines [::cm/out :collisionPoint]
   :LoadImageAnim [::cm/out :frames]
   :ExportImageToMemory [:out :as-array ::cm/return :fileSize]
   :ImageFormat [::cm/mutate :image]
   :ImageToPOT [::cm/mutate :image]
   :ImageCrop [:! :image]
   :ImageAlphaCrop [:! :image]
   :ImageAlphaClear [:! :image]
   :ImageAlphaMask [:! :image]
   :ImageAlphaPremultiply [:! :image]
   :ImageBlurGaussian [:! :image]
   :ImageResize [:! :image]
   :ImageResizeNN [:! :image]
   :ImageResizeCanvas [:! :image]
   :ImageMipmaps [:! :image]
   :ImageDither [:! :image]
   :ImageFlipVertical [:! :image]
   :ImageFlipHorizontal [:! :image]
   :ImageRotate [:! :image]
   :ImageRotateCW [:! :image]
   :ImageRotateCCW [:! :image]
   :ImageColorTint [:! :image]
   :ImageColorInvert [:! :image]
   :ImageColorGrayscale [:! :image]
   :ImageColorContrast [:! :image]
   :ImageColorBrightness [:! :image]
   :ImageColorReplace [:! :image]
   :LoadImagePalette [:out ::cm/return :colorCount]
   :ImageClearBackground [:! :dst]
   :ImageDrawPixel [:! :dst]
   :ImageDrawPixelV [:! :dst]
   :ImageDrawLine [:! :dst]
   :ImageDrawLineV [:! :dst]
   :ImageDrawCircle [:! :dst]
   :ImageDrawCircleV [:! :dst]
   :ImageDrawCircleLines [:! :dst]
   :ImageDrawCircleLinesV [:! :dst]
   :ImageDrawRectangle [:! :dst]
   :ImageDrawRectangleV [:! :dst]
   :ImageDrawRectangleRec [:! :dst]
   :ImageDrawRectangleLines [:! :dst]
   :ImageDraw [:! :dst]
   :ImageDrawText [:! :dst]
   :ImageDrawTextEx [:! :dst]
   :GenTextureMipmaps [:! :texture]
   :LoadFontEx [:in :fontChars :glyphCount]
   :LoadFontFromMemory [[:in :fileData :dataSize] [:in :fontChars :glyphCount]]
   :LoadFontData [[:in :fileData :dataSize] [:in :fontChars :glyphCount] [:out :with-ptr ::cm/return :glyphCount]]
   :GenImageFontAtlas [[:in :chars :glyphCount] [:out :recs :glyphCount]]
   :DrawTriangleStrip3D [:points :pointCount]
   :UploadMesh [:! :mesh]
   :DrawMeshInstanced [:transforms :instances]
   :GenMeshTangents [:! :mesh]
   :LoadMaterials [:out ::cm/return :materialCount]
   :SetMaterialTexture [:! :material]
   :SetModelMeshMaterial [:! :model]
   :LoadModelAnimations [:out ::cm/return :animCount]
   :WaveCrop [:! :wave]
   :WaveFormat [:! :wave]})


(defn generate-raylib-file []
  (ffi/load-library "raylib.dll")
  (->>
   (generate-raylib-header-info)
   (cm/add-generation-info fns-with-pointer-arguments)
   (cm/generate-from-header-info)
   (cm/generate-coffi-file 'raylib)
   (map #(with-out-str (pprint/pprint %)))
   (s/join (println-str))
   (spit "src/raylib.clj")))


(comment

  (generate-raylib-file)

  (def state (atom [(System/nanoTime) [1]]))

  (do

    (init-window 800 450 "raylib-clj [core] example - basic window")
    ;(set-window-state FLAG_VSYNC_HINT)
    (clear-window-state FLAG_VSYNC_HINT)
    ;(set-target-fps 240)
    ;(clear-window-state FLAG_VSYNC_HINT)
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

  (do
    (def state (atom {:last-time (System/nanoTime) :acc []}))
    (def factor 1000000000)
    (rl/InitWindow 1200 800 "raylib-clj [core] example - basic window")
    (rl/SetTargetFPS 10000)
    (let [albedo1 (rl/LoadImage "albedo1.png")
          albedo1 (rl/ImageFormat albedo1 rl/PIXELFORMAT_UNCOMPRESSED_GRAYSCALE)
          texture (rl/LoadTextureFromImage albedo1)
          {:keys [return-value return-value-ptr]} (rl/LoadFileData "test.txt")
          mystr (String. (byte-array return-value))
          _ (rl/UnloadFileData return-value-ptr)
          points (map (partial apply rl/->Vector2)
                      [[100 200]
                       [300 200]
                       [150 100]
                       [150 300]])

          {:keys [collisionPoint]} (apply rl/CheckCollisionLines points)]
      (rl/SetWindowIcon albedo1)
      (while (not (rl/WindowShouldClose))
        (let [{:keys [last-time acc f filedata]} @state
              newtime (System/nanoTime)
              diff (- newtime last-time)
              newacc (vec (take-last 5000 (conj acc diff)))
              average-diff (/ (reduce + newacc) (count newacc))
              average-fps (long (/ 1000000000 average-diff))]
          (swap! state assoc :last-time newtime :acc newacc)
          (rl/BeginDrawing)
          (rl/ClearBackground rl/RAYWHITE)
          (rl/DrawLineStrip points rl/BLACK)
          (rl/DrawText (str "filedata: " (str mystr)) 190 100 20 rl/BLACK)
          (rl/DrawText (str "collisionPoint is: " (update collisionPoint :x identity)) 190 150 20 rl/BLACK)
          (rl/DrawText "Congrats! You created your first raylib window!" 190 200 20 rl/BLACK)
          (rl/DrawText "And you did it from clojure!" (int (+ 190 (* 50 (Math/sin (/ newtime factor))))) 240 20 rl/DARKBLUE)
          (rl/DrawText (str "fps: " average-fps ) 190 380 20 rl/BLACK)
          (rl/DrawTexture texture 300 300 rl/WHITE)
          (rl/EndDrawing))))
    (rl/CloseWindow))

  )


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
