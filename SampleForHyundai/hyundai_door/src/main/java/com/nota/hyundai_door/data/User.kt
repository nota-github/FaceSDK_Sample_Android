package com.nota.hyundai_door.data

import com.nota.nota_sdk.task.vision.FacialProcess
import com.nota.nota_sdk.task.vision.face.FacialFeature

data class User(val id: String, val name: String, val facialData: FacialFeature)