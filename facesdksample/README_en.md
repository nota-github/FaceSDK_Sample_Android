# Sample App using Face SDK<br/>

## Prerequisite
- Nota SDK 
> SDK(.aar) should be added to src/main/libs and defined in build.gradle.
The sample application will fail to launch without SDK.

<br/><br/>

## Usage
Sample App is consists of 'Inference Mode' and 'Camera Mode'.
- Inference mode: applies face recognition on a singular bitmap
- Camera mode: applies real-time face recognition from the camera frame

<br/>

### Inference Mode
Applies face recognition to the images in the Assets/faces.

<img src="https://user-images.githubusercontent.com/75300554/142960540-a1e79398-0549-4c3f-afba-e41bd13fcf48.jpg" width="300" height="500"/>

<br/>

#### Description :
- (1) Mode Button - switch between Inference and Camera mode.
- (2) Thread Strategy - Select thread strategy to run face recognition process :
> UI thread (Main Thread) : 
> Shows the best performance, but blocks UI from being updated while recognition is taking place
> - Default Thread : Newly constructed thread (no priority given)
> Does not affect UI update, but recognition latency may vary depending on the device
> - High Priority Thread: Newly constructed thread with high priority  
> Does not affect UI update, recognition may vary depending on the device but will out-perform running the process on default-thread
- (3) Next/Prev Image Button: Change target image to its next/previous image to run face recognition
- (4) Inference Time Log - Logs time elapsed for face detection and face extraction
- (5) Overlay Log - Prints bounding box and id of the face within the image (if registered)<br/><br/>

### Camera Mode
Run real time face recognition to the incoming frame from the camera

<img src="https://user-images.githubusercontent.com/75300554/142960546-fcd51e1f-0e54-4535-9ff8-3ee139371222.jpg" width="300" height="500"/>

#### Description:
- (1) Mode Button - Switch between Inference and Camera mode.
- (2) Thread Strategy - Select thread strategy to run face recognition process :
> UI thread (Main Thread) : 
> Shows the best performance, but blocks UI from being updated while recognition is taking place
> - Default Thread : Newly constructed thread (no priority given)
> Does not affect UI update, but recognition latency may vary depending on the device
> - High Priority Thread: Newly constructed thread with high priority  
> Does not affect UI update, recognition may vary depending on the device but will out-perform running the process on default-thread
- (3) Camera Switch Button: Changes camera facing
- (4) Feature Extract Checkbox: Extracts facial features if checked
- (5) Registration Button: Saves the extracted facial feature to the local database
- (6) Inference Time Log - Logs time elapsed for face detection and face extraction
- (7) Overlay Log - Prints bounding box and id of the face within the image (if registered)

