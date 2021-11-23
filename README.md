# Nota Face SDK for Android
노타의 안드로이드용 안면인식 SDK

## License
노타에서 제공하는 데모 라이센스는 3개월 시용기간을 제공하고 있다  

  
## Features
SDK에서 제공되는 기능은 아래와 같다 :
* 얼굴감지 : 이미지에서 얼굴의 BoundingBox 및 Landmark를 추출한다.
* 안면인식 : 얼굴을 인식해 특징을 추출. 특징은 float array로 표현된다.
* 특징비교 : 2개의 얼굴 특징을 비교해 유사도를 도출한다.


## Prerequisite
1. OpenCV (4.4.0 버전 권장)
2. Tensorflow lite (https://www.tensorflow.org/lite/guide/android)  
  
    
## Usage
노타 안면인식 SDK는 아래와 같은 순서로 작동한다 : 
1. [`SDK 초기화`](#initialization)
2. [`FacialProcess` 선언 및 inference 메서드 호출](#FacialProcess)
3. callback 메서드 파라미터로 결과값 도출 -> 도출된 특징 저장
4. [(저장된)안면 특징들을 비교해 `유사도 도출`](#featurecomparison)
  
   
     
### Sample 코드
```kotlin
class Sample {
    // SDK initialization
    init{
        NotaFaceSDK.initialize(context, key)
    } 
    
    ...
    
    // Call FacialProces.inference()
    FacialProcess.inference(inputBitmap, isFaceRecognition) { results ->
          // TODO: Handling of extracted facial features
    }
    
    ...
    
    // Compare features and get degree of similarity
    private fun compareFeatures(@NonNull feature1 : FloatArray, @NonNull feature2 : FloatArray) : Double {
        return feature1.isSimilar(feature2)
    }
    
    ...
}

```
  
### Initialization
SDK 사용을 위해선, SDK 초기화가 우선적으로 이뤄져야한다. SDK 초기화 없이 활용되는 기능오류를 발생시킬 수 있다.  
(유료 라이센스 기준) SDK 초기화를 위해선, 사전에 Nota에서 발급한 라이센스 키를 입력하여야 한다.  
(데모 라이센스 기준) 사전에 협의가 된 기간내에만 SDK 초기화가 가능하다

```kotlin
NotaFaceSDK.initialize(context, key)
```
  
### FaceRecognition
FaceRecognition을 사용하기 위해선 [`SDK 초기화`](#initialization)가 되어 있어야 한다.  
안면인식에 대한 결과는 FacialProcess.inference 메서드의 callback 메서드 파라미터를 이용해 얻어올 수 있다.  
성공적인 안면인식이 된 경우 [List<FacialProcess.Result>](#Result) 객체가 반환된다  
얼굴이 감지되지 않는 경우 emptyList() 가 반환된다.  
  
  
#### FacialProcess.inference()

```kotlin

fun inference(inputBitmap: Bitmap, isFaceRecognition: Boolean, callback:(result: List<Result>)->Unit)

```
  
  
## Data
### Result
Result 클래스는 안면인식 SDK 에서 사용하는 데이터 클래스이다.  
inference 메서드 호출시 isFaceRecognition 파라미터를 통해, 얼굴 특징 추출 기능을 on/off 할 수 있다.  
얼굴 특징 추출 기능이 off 된 경우, Result 객체의 FacialFeature 값이 null로 반환된다.  
[Face](#face) 객체 에는 눈, 코, 등의 좌표와 얼굴 Bounding box의 위치가 있다.  
```kotlin
data class Result(val face: Face, val facialFeature: FacialFeature?, val detectedFaceBitmap: Bitmap, val log: Log)
data class Log(val fdInferenceTime: Long, val frInferenceTime: Long?)
```


### Face
Face 데이터 클래스
```kotlin
data class Face(@NonNull val loc : RectF, @NonNull val landmarks : List<PointF>)
```

#### Feature Comparison
두개의 feature를 비교해 유사도를 도출한다. 권장하는 유사도는 0.65점으로, 권장 유사도 이상의 유사도를 지닌 두개의 얼굴은 동일한 인물이라고 판별하는걸 권장한다.

```kotlin
/**
 * If smilarity is above that of threshold, then we consider two features to be identical
 */
fun isIdentical(var1 : FacialFeature, threshold : Double = 0.65) : Boolean

/**
 * Gets similarity between two facial features
 */
fun getSimilarity(var1 : FacialFeature) : Double

```

## Exception
안면인식 SDK에서 발생할 수 있는 Exception들을 아래와 같다  

1. IllegalLicenseException 
   - 개발자가 SDK 초기화시 제공한 라이센스가 유효하지 않거나 사용기간이 초과한 경우에 발생하는 에러이다
2. OutOfMemoryError
   - 안면인식 모델들 로딩할때 메모리가 부족해 발생하는 에러이다. Manifest에 android:largeHeap="true"을 정의하여 해결할 수 있다
3. ClassNotFoundException: Didn't find class "...."
   - 프로젝트에 추가되어야 하는 api 들이 정의되어 있지 않아 발생하는 에러이다. [필수 api](#Prerequisite)
