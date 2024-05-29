import os
import subprocess

def apply_patch():
    # Đường dẫn tới tệp patch và thư mục đích
    patch_file = 'remove_noexcept.patch'
    target_dir = 'kivy/core/image/_img_sdl2.pyx'

    # Kiểm tra xem tệp patch có tồn tại không
    if os.path.isfile(patch_file):
        # Áp dụng patch
        result = subprocess.run(['patch', target_dir, '<', patch_file], shell=True)
        if result.returncode == 0:
            print("Patch applied successfully.")
        else:
            print("Failed to apply patch.")
    else:
        print(f"Patch file {patch_file} not found.")

if __name__ == '__main__':
    apply_patch()
