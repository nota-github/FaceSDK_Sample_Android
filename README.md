# Face Recognition SDK - Android

## Introduction
This SDK provides APIs and a sample application for real-time face recognition 

## License
Demo license allows up to 3 months free trial

## Features
Features provided in this SDK are as follows :
* Face Detection: detects face from the image and extract its bounding box and landmarks
* Face Recognition: recognizes a face via extracting its feature. Features are expressed in float array
* Feature Comparison: calculates similarity between one feature to another using cosine similarity

## Prerequisite
1. OpenCV 
   recommended version : >= 4.4.0)
2. Tensorflow and Tensorflow-lite (https://www.tensorflow.org/lite/guide/android) 
   recommened version : nightly 

- [Sample Application](https://github.com/nota-github/Nota_FaceSDK_Sample_Android/tree/main/facesdksample) <br />
please refer to build.gradle

![스크린샷 2021-11-23 오전 11 33 09](https://user-images.githubusercontent.com/75300554/142963202-2e5560c2-0b1b-4cca-8c16-ccbf8013f9d1.png) <br />
    
## Usage
To implement face recognition using this SDK, follow the steps below:
1. [`SDK initialization`](#initialization)
2. [`FacialProcess` declaration and call inference method](#FacialProcess)
3. Attach callback to retrieve the output of the FacialProcess
4. [Compare features using the provided method 'feature comparison'](#featurecomparison)
  
   
     
### Sample Code
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
SDK initialization must be done before any usage. Use of the SDK without initialization may and will invoke errors or unexpected results that Nota will not be held responsible for.
(Paid License) Provide License Key issued for SDK initialization.
(Demo License) SDK will only be available for the pre-negotiated duration

```kotlin
NotaFaceSDK.initialize(context, key)
```
  
### FaceRecognition
To use FaceRecogntion, [`SDK initialization`](#initialization) must be performed.  
The result of FaceRecogition can be retrieved by attaching the callback method to FacialProcess.inference
Upon successful Face Recognition, [List<FacialProcess.Result>](#Result) will be returned via the callback
If no face is detected, emptyList() will be returned.  
  
#### FacialProcess.inference()

```kotlin

fun inference(inputBitmap: Bitmap, isFaceRecognition: Boolean, callback:(result: List<Result>)->Unit)

```
  
  
## Data
### Result
Result is a pre-defined data class used in the SDK.  
Feature extraction can be turned on/off by setting isFaceRecognition parameter when using the inference method
If feature extraction is turned off, the FacialFeature value of the Result class will be null.
[Face](#face) contains coordinates and bounding box of the face and its landmarks
```kotlin
data class Result(val face: Face, val facialFeature: FacialFeature?, val detectedFaceBitmap: Bitmap, val log: Log)
data class Log(val fdInferenceTime: Long, val frInferenceTime: Long?)
```


### Face
Face
```kotlin
data class Face(@NonNull val loc : RectF, @NonNull val landmarks : List<PointF>)
```

#### Feature Comparison
Calculates similarity between two features. Two features with similarity above the threshold are considered identical. Recommended similarity threshold is 0.65.

```kotlin
/**
 * If the similarity is above that of the threshold, then we consider two features to be identical
 */
fun isIdentical(var1 : FacialFeature, threshold : Double = 0.65) : Boolean

/**
 * Gets similarity between two facial features
 */
fun getSimilarity(var1 : FacialFeature) : Double

```

## Exception
Exceptions that may occur when using the SDK are as follow : 

1. IllegalLicenseException 
   - Invoked when the key given during the SDK initialization is either invalid or expired
2. OutOfMemoryError
   - Invoked when memory is insufficient when loading the ai model. Allow android:largeHeap="true" on the application Manifest to resolve the problem
3. ClassNotFoundException: Didn't find class "...."
   - Invoked when prerequisite APIs are not implemented. [prerequisite api](#Prerequisite)
