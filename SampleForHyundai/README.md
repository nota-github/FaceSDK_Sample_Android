# Nota Face SDK for Android
노타의 안드로이드용 안면인식 SDK

## License
노타에서 제공하는 데모 라이센스는 3개월 시용기간을 제공하고 있다  

  
## Features
SDK에서 제공되는 기능은 아래와 같다 :
* 얼굴감지 : 이미지에서 얼굴을 인식, 얼굴의 위치와 눈코입의 위치, 마스크 착용여부, Spoofing 여부 등을 검출한다.
* 얼굴특징추출 : 검출된 얼굴 이미지에서 특징 추출을 추출한다. 
* 특징비교 : 2개의 얼굴 특징을 비교해 유사도를 도출한다


## Prerequisite
1. OpenCV (버전은 상관없음)
2. Tensorflow lite (https://www.tensorflow.org/lite/guide/android)  
  
    
## Usage
노타 안면인식 SDK는 아래와 같은 순서로 작동한다 : 
1. [`SDK 초기화`](#initialization)
2. `FacialProcess` 객체의 detectFace() 메서드 사용하여 얼굴 이미지 추출
3. `FacialProcess` 객체의 featureExtract() 메서드에 FaceRecognition에 얼굴 이미지 입력 -> 도출된 특징 저장
4. (저장된)안면 특징들을 비교해 [`유사도 도출`](#featurecomparison)
  
   
     
### Sample 코드
```kotlin
class Sample {
    // SDK initialization
    init{
        NotaFaceSDK.initialize(context)
    } 
    
    ...
    
    // Detect Face
    FacialProcess.detectFace(bitmap, inferenceOption) { result ->
                
    ...
    
    }
    
    // Extract features from a given bitmap
    FacialProcess.featureExtract(detectedFaceBitmap) { result ->
    
    ...
    
    }
    
    // Compare features and get degree of similarity
    if( facialFeature.isIdentical(anotherFaicalFeature) ) {
    
    // isCorrect
    ...
    
    }
    
}

```
  
### Initialization
SDK 사용을 위해선, SDK 초기화가 우선적으로 이뤄져야한다. SDK 초기화 없이 활용되는 기능오류를 발생시킬 수 있다.  
(유료 라이센스 기준) SDK 초기화를 위해선, 사전에 Nota에서 발급한 라이센스 키를 입력하여야 한다.  
(데모 라이센스 기준) 사전에 협의가 된 기간내에만 SDK 초기화가 가능하다.  

```kotlin
NotaFaceSDK.initialize(context, key)
```
  
### Detect Face
Parameter 로 전달된 Bitmap 타입의 이미지에서 얼굴을 검출하는 Task 를 수행한다.  
Detect Face 메서드를 사용하기 위해선 [`SDK 초기화`](#initialization)가 되어 있어야 한다.  
Option 객체를 Parameter 로 전달하여 얼굴 인식에 소요되는 시간을 조절할 수 있다.  
안면인식에 대한 결과는 callback 람다 함수를 이용해 얻어올 수 있다.  
성공적인 안면인식이 된 경우 [FacialProcess.FaceDetectResult](#facialProcess.facedetectresult) 객체가 반환된다.
  
```kotlin
private val inferenceOption =
                FacialProcess.Option(
                    isCheckBlurScore = true,
                    isDetectMask = true,
                    isDetectSpoof = true
                )

FacialProcess.detectFace(bitmap, inferenceOption) { result ->
/**
 * Attaching callbacks to the Detect Face Task
 */
 
  ...

}
```
  
### Feature Extract
Parameter 로 전달된 Bitmap 타입의 이미지에서 얼굴의 특징을 추출하는 Task 를 수행한다.  
Feature Extract 메서드를 사용하기 위해선 [`SDK 초기화`](#initialization)가 되어 있어야 한다.  
Input Parameter 는 [Detect Face](#detect-face) 메서드의 Callback 리턴값인. 
[FacialProcess.FaceDetectResult](#facialProcess.facedetectresult) 객체의 detectedFaceBitmap 을 사용하는 것을 권장한다.  
얼굴 특징 추출 결과는 callback 람다 함수를 이용해 얻어올 수 있다.  
얼굴 특징이 완료된 경우 [FacialProcess.FeatureExtractResult](#facialProcess.featureextractresult) 객체가 반환된다.  

```kotlin
FacialProcess.featureExtract(faceDetectResult.detectedFaceBitmap){ result ->

  /**
   * Attaching callbacks to the Feature Extract Task
   */

}
```

## Data

### FacialProcess.FaceDetectResult
FaceDetectResult은 안면인식 SDK 에서 사용하는 데이터 클래스이다.  
FacialProcess.Option 생성시 부여한 옵션에 의해 몇몇 값들 (설정값이 false인 경우)은 null로 반환될 수가 있다.  
[Face](#face) 에는 눈, 코, 등의 좌표와 얼굴 Bounding box의 위치가 있다.  
```kotlin
data class FaceDetectResult(val face: Face?, val detectedFaceBitmap: Bitmap, val isSpoof:Boolean? = null, val isMaskDetected:Boolean? = null, val faceQuality:Double? = null, val blurScore:Double?=null, val inferenceTimeLog: Long)

```

### Face
검출된 얼굴의 정보가 담긴 데이터 클래스이다.  
loc 객체는 얼굴의 위치, landmarks 객체는 얼굴의 눈 코 입 위치 정보를 가지고 있다.  

```kotlin
data class Face(@NonNull val loc : RectF, @NonNull val landmarks : List<PointF>)
```

### FacialProcess.FeatureExtractResult
FacialProcess.FeatureExtractResult은 안면인식 SDK 에서 사용하는 데이터 클래스이다.  
얼굴 유사도 검출에 사용하는 [FacialFeature](#featurecomparison) 객체를 반환한다.  
```kotlin
data class FeatureExtractResult(val facialFeature: FacialFeature, val inferenceTimeLog: Long)
```


#### FeatureComparison
두개의 feature를 비교해 유사도를 도출한다.  
권장하는 유사도는 0.65점으로, 권장 유사도 이상의 유사도를 지닌 두개의 얼굴은 동일한 인물이라고 판별하는걸 권장한다.  

```kotlin
/**
 * If smilarity is above that of threshold, then we consider two features to be identical
 */
FacialFeature.fun isIdentical(var1 : FacialFeature, threshold : Double = 0.65) : Boolean

/**
 * Gets similarity between two facial features
 */
FacialFeature.fun getSimilarity(var1 : FacialFeature) : Double

```

## Exception
안면인식 SDK에서 발생할 수 있는 Exception들을 아래와 같다  

1. IllegalLicenseException 
   - 개발자가 SDK 초기화시 제공한 라이센스가 유효하지 않거나 사용기간이 초과한 경우에 발생하는 에러이다
2. OutOfMemoryError
   - 안면인식 모델들 로딩할때 메모리가 부족해 발생하는 에러이다. Manifest에 android:largeHeap="true"을 정의하여 해결할 수 있다
3. ClassNotFoundException: Didn't find class "...."
   - 프로젝트에 추가되어야 하는 api 들이 정의되어 있지 않아 발생하는 에러이다. [필수 api](#prerequisite)
