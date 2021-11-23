# Nota Face SDK Sample App For Android
노타의 안드로이드용 Face SDK Sample App

## Prerequisite
- Nota SDK 


제공된 Nota SDK 파일(.aar)을 src/main/libs 폴더에 넣고 build.gradle 에 추가해야 한다.  
SDK 파일이 없을 시, 샘플 앱 실행이 불가능하다.


## Usage
Nota SDK Sample App은 카메라 없이 이미지에서 얼굴 인식 프로세스를 진행하는 'Inference Mode'와,  
카메라를 통해 들어오는 이미지에서 얼굴 인식 프로세스를 진행하는 'Camera Mode' 를 구현하였다.

### Inference Mode
Assets/faces 폴더에 있는 이미지를 로드하여 얼굴 특징 추출 및 유사도 검사 프로세스를 수행한다.

<img src="https://user-images.githubusercontent.com/75300554/142960540-a1e79398-0549-4c3f-afba-e41bd13fcf48.jpg" width="300" height="500"/>

#### 화면 설명:
- (1) 모드 변경 - Inference Mode, Camera Mode 를 변경할 수 있다.
- (2) Thread Strategy 변경 - Inference를 수행할 Thread를 변경할수 있다.  
> - UI Thread (Main Thread) : Inference를 UI(Main) Thread 에서 수행한다.  
프로세스 처리 시간이 빠르지만, 프로세스가 처리되는 동안 UI가 갱신되지 않는 현상이 일어난다.  
> - Defualt Thread(Thread 생성 및 Default Priority로 설정) : Inference를 새 Thread를 생성하여 처리한다.  
프로세스 처리와 관계없이 UI가 갱신되지만, 기기에 따라 프로세스 처리 시간이 느려질 수 있다.
> - High Priority Thread(Thread 생성 및 Process와 Thread Proirity 를 높게 설정) :  
Inference를 새 Thread를 생성하고 현재 Process 및 Thread의 Proirity 를 강제로 높게 설정한다.   
프로세스 처리와 관계없이 UI가 갱신된다. 기기에 따라 Default Thread에 비해 프로세스 처리시간이 빨라질 수 있다.
- (3) 다음 이미지, 이전 이미지로 변경 버튼
- (4) Inference Time Log - 각각 얼굴 검출, 얼굴 특징 추출에 소모된 시간을 출력한다.
- (5) Overlay Log - 검출된 얼굴의 Bounding Box 및 등록된 FacialFeature의 id를 출력한다.


### Camera Mode
Camera에서 받아온 이미지를 통해 얼굴 특징 추출 및 유사도 검사 프로세스를 수행한다.

<img src="https://user-images.githubusercontent.com/75300554/142960546-fcd51e1f-0e54-4535-9ff8-3ee139371222.jpg" width="300" height="500"/>

#### 화면 설명:
- (1) 모드 변경 - Inference Mode, Camera Mode 를 변경할 수 있다.
- (2) Thread Strategy 변경 - Inference를 수행할 Thread를 변경할수 있다.  
> - UI Thread (Main Thread) : Inference를 UI(Main) Thread 에서 수행한다.  
프로세스 처리 시간이 빠르지만, 프로세스가 처리되는 동안 UI가 갱신되지 않는 현상이 일어난다.  
> - Defualt Thread(Thread 생성 및 Default Priority로 설정) : Inference를 새 Thread를 생성하여 처리한다.  
프로세스 처리와 관계없이 UI가 갱신되지만, 기기에 따라 프로세스 처리 시간이 느려질 수 있다.
> - High Priority Thread(Thread 생성 및 Process와 Thread Proirity 를 높게 설정) :  
Inference를 새 Thread를 생성하고 현재 Process 및 Thread의 Proirity 를 강제로 높게 설정한다.   
프로세스 처리와 관계없이 UI가 갱신된다. 기기에 따라 Default Thread에 비해 프로세스 처리시간이 빨라질 수 있다.
- (3) 카메라 변경 - 전면. 후면 카메라를 변경한다.
- (4) Feature Extract CheckBox - 얼굴 특징 추출을 수행할것인지 체크한다
- (5) Registration 버튼 - 추출된 Facial Feature 를 내부 DB에 등록한다.
- (6) Inference Time Log - 얼굴 검출, 얼굴 특징 추출에 소모된 시간과 얼굴 검출 및 추출에 소모된 시간에 대한 FPS를 출력한다.
- (7) Overlay Log - 검출된 얼굴의 Bounding Box, Landmark 및 등록된 FacialFeature의 id를 출력한다.


