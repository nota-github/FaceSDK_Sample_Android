
# Nota Face SDK for Android
This library allows you to integerate Nota's Face Recognition API into Android application
  
## Features
Features provided by the SDK are as follows :
* Face recognition - Recognizes given face and returns its facial features
* Feature comparison - Compares two facial features and returns the degree of similarity between them
  
## Usage
In order to use Nota's Face SDK, steps below must be followed in order :
1. [`SDK initailization`](#initialization)
3. [`FaceRecognition` instantiation](#FaceRecognition)
4. Input image with a face to the FaceRecognition instantiated
5. [Compare features and get `degree of similarity`](#featurecomparison)
  
### Sample
```kotlin
class Sample {
    init{
        NotaFaceSDK.initialize(context, key)
    } 
    
    ...
    
    private val faceRecognitionTask = FaceRecognitionTask.getClient(
                    FaceRecognitionOption.Builder().build())
                
    ...
    
    private fun recognizeFace(@NonNull var1 : Bitmap) : FloatArray {
        return faceRecognition.recognize(var1)
    }
    
    ...

    private fun compareFeatures(@NonNull feature1 : FacialFeature, @NonNull feature2 : FacialFeature) : Boolean {
        return feature1.isIdenticalTo(threshold, feature2)
    }
}

```
  
### Initialization
SDK initalization must occur prior to any use of SDK. 
To initialize SDK, developer must provide key issued by Nota. 

```kotlin
NotaFaceSDK.initialize(context, key)
```
  
### FaceRecognition
[`SDK initailization`](#initialization) must occur prior to FaceRecognition instantiation  
FaceRecognition can be constructed with couple of options provided by the SDK (Detailed info of the options can be found [here](#facerecognitionoption))  
Results of face recognition can be retrieved via attaching callbacks to the object. If operation is successfully executed, a [Recognition](#recognition) object will be returned
  
```kotlin
private val recogOption = FaceRecognitionOption.Builder().build()

val faceRecognitionTask = FaceRecognitionTask.getClient(recogOption)

/**
 * Attaching callbacks to the FaceRecognitionClient
 */
 ...
 
 faceRecognitionClient.addOnSuccessListener(object : OnSuccessListener<Recognition>{
    override fun onSuccess(var1 : Recognition) {
       /// TODO apply custom operations once results are obtained
    }
 })
 
 faceRecognitionClient.addOnFailureListener(object : OnFailureListener{
    override fun onFailure(t : Throwable) {
        // TODO handle error
    }
 })
 
 ...
 
```
  
#### FaceRecognitionOption

```kotlin
val faceRecognitionOption = FaceRecognitionOption.Builder().build()

```
  
  
## Data
### Recognition
Recognition is data class used within SDK for FaceRecognition. Some of its values may be null depending on the FaceRecogitionOption given and inference result.
[Face](#face) within Recognition class contains the location of the face and its landmarks. 
```kotlin
data class Recognition( var face           : Face? = null,
                        var feature        : FacialFeature? = null,
                        val originalBitmap : Bitmap)
```


### Face
Face is data class used within SDK for face detection. 
```kotlin
data class Face( val loc : RectF,  val landmarks : List<PointF>){
    internal fun getLandmarks() : DoubleArray{
        return doubleArrayOf(
            landmarks.get(0).x.toDouble(), landmarks.get(0).y.toDouble(),  // left eye
            landmarks.get(1).x.toDouble(), landmarks.get(1).y.toDouble(),  // right eye
            landmarks.get(2).x.toDouble(), landmarks.get(2).y.toDouble(),  // nose
            landmarks.get(3).x.toDouble(), landmarks.get(3).y.toDouble(),  // mouth left
            landmarks.get(4).x.toDouble(), landmarks.get(4).y.toDouble() // mouth right
        )
    }
}
```

#### Feature Comparison
Compares two features(float array) and gives similarity score. Recommended threshold for similarity is `0.65`  

```kotlin
fun compareFeatures(feature01 : FloatArray, feature02 : FloatArray) : Double

```

## License
To view the full license, visit (http://github.com)

