from kivy.lang import Builder
from kivy.uix.relativelayout import RelativeLayout
from kivymd.app import MDApp
from kivy.uix.label import Label
from kivy.uix.camera import Camera

kv_string = '''
RelativeLayout:
    Camera:
        id: camera
        resolution: (640, 480)
        play: True
        index: 0  # Thay đổi chỉ số camera nếu cần
        size_hint: None, None
        size: root.width, root.height
        pos_hint: {'center_x': 0.5, 'center_y': 0.5}
    Label:
        id: no_detect_label
        text: "No detect"
        halign: 'center'
        valign: 'middle'
        font_size: '24sp'
        size_hint: None, None
        size: self.texture_size
        pos_hint: {'center_x': 0.5, 'center_y': 0.5}
        color: 1, 0, 0, 1  # Màu đỏ cho dễ nhìn
'''

class DrowsinessApp(MDApp):
    def build(self):
        return Builder.load_string(kv_string)

if __name__ == '__main__':
    DrowsinessApp().run()
