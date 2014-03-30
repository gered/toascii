(ns toascii.models.flf
  (:require [clojure.java.io :as io]
            [clj-figlet.core :refer [load-flf]]
            [taoensso.timbre :refer [log]]
            [toascii.util :refer [get-filename-without-ext]]))

(def flf-files
  ["3-d.flf"
   "3x5.flf"
   "5lineoblique.flf"
   "acrobatic.flf"
   "alligator.flf"
   "alligator2.flf"
   "alphabet.flf"
   "avatar.flf"
   "banner.flf"
   "banner3-D.flf"
   "banner3.flf"
   "banner4.flf"
   "barbwire.flf"
   "basic.flf"
   "bell.flf"
   "big.flf"
   "bigchief.flf"
   ;"binary.flf"   ; this is kind of in a weird format, clj-figlet fails to load
   "block.flf"
   ;"bubble.flf"   ; probably an issue with some "other chars" having the char-code line only having a number (no desc string)
   "bulbhead.flf"
   ;"calgphy2.flf"   ; dunno why it fails
   "caligraphy.flf"
   "catwalk.flf"
   "chunky.flf"
   "coinstak.flf"
   "colossal.flf"
   "computer.flf"
   "contessa.flf"
   "contrast.flf"
   "cosmic.flf"
   "cosmike.flf"
   "cricket.flf"
   "cursive.flf"
   "cyberlarge.flf"
   "cybermedium.flf"
   "cybersmall.flf"
   "diamond.flf"
   ;"digital.flf"   ; likely failing due to "other char" char-code lines having only a number
   "doh.flf"
   "doom.flf"
   "dotmatrix.flf"
   ;"drpepper.flf"  ; "other char" char-code lines having only a number
   "eftichess.flf"
   ;"eftifont.flf"  ; "other char" char-code lines having only a number
   ;"eftipiti.flf"  ; "other char" char-code lines having only a number
   "eftirobot.flf"
   ;"eftitalic.flf" ; "other char" char-code lines having only a number
   "eftiwall.flf"
   "eftiwater.flf"
   "epic.flf"
   "fender.flf"
   "fourtops.flf"
   "fuzzy.flf"
   "goofy.flf"
   "gothic.flf"
   "graffiti.flf"
   "hollywood.flf"
   "invita.flf"
   "isometric1.flf"
   "isometric2.flf"
   "isometric3.flf"
   "isometric4.flf"
   "italic.flf"
   "ivrit.flf"
   "jazmine.flf"
   "jerusalem.flf"
   "katakana.flf"
   "kban.flf"
   "larry3d.flf"
   "lcd.flf"
   "lean.flf"
   "letters.flf"
   "linux.flf"
   "lockergnome.flf"
   "madrid.flf"
   "marquee.flf"
   "maxfour.flf"
   "mike.flf"
   "mini.flf"
   ;"mirror.flf"    ; "other char" char-code lines having only a number
   ;"mnemonic.flf"  ; regex for "other char" char-code line will fail a lot due to more then 2 spaces between code and desc string
   ;"morse.flf"     ; "other char" char-code lines having only a number
   "moscow.flf"
   "nancyj-fancy.flf"
   "nancyj-underlined.flf"
   "nancyj.flf"
   "nipples.flf"
   "ntgreek.flf"
   "o8.flf"
   "ogre.flf"
   "pawp.flf"
   "peaks.flf"
   "pebbles.flf"
   "pepper.flf"
   "poison.flf"
   "puffy.flf"
   ;"pyramid.flf"    ; "other char" char-code lines having only a number
   "rectangles.flf"
   "relief.flf"
   "relief2.flf"
   "rev.flf"
   "roman.flf"
   ;"rot13.flf"      ; "other char" char-code lines having only a number
   "rounded.flf"
   "rowancap.flf"
   "rozzo.flf"
   "runic.flf"
   "runyc.flf"
   "sblood.flf"
   "script.flf"
   "serifcap.flf"
   "shadow.flf"
   "short.flf"
   "slant.flf"
   "slide.flf"
   "slscript.flf"
   "small.flf"
   "smisome1.flf"
   "smkeyboard.flf"
   "smscript.flf"
   "smshadow.flf"
   "smslant.flf"
   "smtengwar.flf"
   ;"speed.flf"        ; "other char" char-code lines having only a number
   ;"stampatello.flf"  ; "other char" char-code lines having only a number
   "standard.flf"
   "starwars.flf"
   "stellar.flf"
   "stop.flf"
   "straight.flf"
   "tanja.flf"
   "tengwar.flf"
   ;"term.flf"         ; "other char" char-code lines having only a number
   "thick.flf"
   "thin.flf"
   "threepoint.flf"
   "ticks.flf"
   "ticksslant.flf"
   "tinker-toy.flf"
   "tombstone.flf"
   "trek.flf"
   "tsalagi.flf"
   "twopoint.flf"
   "univers.flf"
   "usaflag.flf"
   "wavy.flf"
   "weird.flf"])

(defonce fonts (atom {}))

(defn get-font [name]
  (get @fonts name))

(defn get-font-names []
  (->> @fonts
       (keys)
       (sort)))

(defn load-all! []
  (->> flf-files
       (reduce
         (fn [loaded filename]
           (log :info "loading flf font:" filename)
           (assoc loaded (get-filename-without-ext filename) (load-flf (io/resource (str "flf/" filename)))))
         {})
       (reset! fonts)
       (count)))