# Drowsiness Detection App 📱

This project is a KivyMD-based application for detecting drowsiness. Follow the steps below to set up the development environment and install the necessary dependencies.

## Prerequisites

- Python 3.6 or higher
- pip (Python package installer)


## Setup Instructions

### 1. Clone the Repository

First, clone the repository to your local machine:

```sh
git clone https://github.com/ImmChad/drowsiness-detection-app-project.git
```

### 2. Install Library

#### 1. Create a virtual environment
Create a virtual environment in the project directory and activate it. This step ensures that all dependencies are installed in an isolated environment.

**On Windows**

```sh
# if it doesn't have venv folder, use code
python -m venv .venv
# use this code to create a virtual environment
.\.venv\Scripts\Activate
```

**On macOS/Linux**

```sh
# if it doesn't have venv folder, use code
python -m venv .venv
# use this code to create a virtual environment
source .venv/bin/activate
```

#### 2. Install Required Libraries

Once the virtual environment is activated, install the necessary libraries using pip:

**📑 Kivy library**
```sh
pip install kivy kivymd
```

**📑 OpenCV library**
```sh
pip install opencv-python
```

## Run the Application

Run the application to ensure everything is set up correctly:

```sh
python main.py
```

**📴 Deactivating the Virtual Environment**
```sh
deactivate
```

## Export the Application

You can export the mobile app here: 
https://colab.research.google.com/drive/1NlODsndZX_clE6kLLanciBUono84CnK9#scrollTo=SXfAWrCMp2n_

Export the mobile app: 

Reference link: https://buildozer.readthedocs.io/en/latest/installation.html


First, install the buildozer project with:
```sh
pip3 install --user --upgrade buildozer
```

If you use window, you have to use Wsl or ubuntu terminal. Or you should use Ubuntu 20.04 and 22.04 (64bit)
```sh
sudo apt update
sudo apt install -y git zip unzip openjdk-17-jdk python3-pip autoconf libtool pkg-config zlib1g-dev libncurses5-dev libncursesw5-dev libtinfo5 cmake libffi-dev libssl-dev
pip3 install --user --upgrade Cython==0.29.33 virtualenv
```



## Author
![Kenny Truong](/assets/images/KENNY.JPG)
Kenny Truong - https://github.com/ImmChad

