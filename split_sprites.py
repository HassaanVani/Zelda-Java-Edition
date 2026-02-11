"""
Sprite Splitter & Rotator for Zelda NES
Splits animated GIFs into numbered PNGs and generates 4 rotations.
Output: sprites_split/<original_name>/0.png, 1.png, ..., plus _up.png, _down.png, _left.png, _right.png
"""
import os
import sys
from PIL import Image

INPUT_DIR = "sprites"
OUTPUT_DIR = "sprites_split"

def process_gif(gif_path, rel_path):
    name = os.path.splitext(os.path.basename(gif_path))[0]
    out_dir = os.path.join(OUTPUT_DIR, os.path.dirname(rel_path), name)
    os.makedirs(out_dir, exist_ok=True)

    try:
        img = Image.open(gif_path)
    except Exception as e:
        print(f"  SKIP {gif_path}: {e}")
        return

    frame_idx = 0
    frames = []
    try:
        while True:
            frame = img.copy().convert("RGBA")
            frames.append(frame)

            base_path = os.path.join(out_dir, f"{frame_idx}.png")
            frame.save(base_path)

            # down = original (front-facing default)
            frame.save(os.path.join(out_dir, f"{frame_idx}_down.png"))

            # left = original (left-facing, no change needed)
            frame.save(os.path.join(out_dir, f"{frame_idx}_left.png"))

            # right = horizontal flip
            flipped_h = frame.transpose(Image.FLIP_LEFT_RIGHT)
            flipped_h.save(os.path.join(out_dir, f"{frame_idx}_right.png"))

            # up = vertical flip (approximation for back view)
            flipped_v = frame.transpose(Image.FLIP_TOP_BOTTOM)
            flipped_v.save(os.path.join(out_dir, f"{frame_idx}_up.png"))

            frame_idx += 1
            img.seek(img.tell() + 1)
    except EOFError:
        pass

    print(f"  {rel_path}: {frame_idx} frame(s) -> {out_dir}")

def main():
    input_dir = INPUT_DIR
    if len(sys.argv) > 1:
        input_dir = sys.argv[1]

    print(f"Scanning {input_dir} for GIF files...")
    count = 0

    for root, dirs, files in os.walk(input_dir):
        for f in sorted(files):
            if f.lower().endswith(".gif"):
                full_path = os.path.join(root, f)
                rel_path = os.path.relpath(full_path, input_dir)
                process_gif(full_path, rel_path)
                count += 1

    print(f"\nDone. Processed {count} GIF files into {OUTPUT_DIR}/")

if __name__ == "__main__":
    main()
