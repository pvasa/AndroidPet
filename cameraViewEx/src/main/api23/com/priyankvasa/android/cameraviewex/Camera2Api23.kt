/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.priyankvasa.android.cameraviewex

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.params.StreamConfigurationMap
import android.os.Build
import kotlinx.coroutines.Job

@TargetApi(Build.VERSION_CODES.M)
internal open class Camera2Api23(
    listener: CameraInterface.Listener,
    preview: PreviewImpl,
    config: CameraConfiguration,
    job: Job,
    context: Context
) : Camera2(listener, preview, config, job, context) {

    override fun collectPictureSizes(sizes: SizeMap, map: StreamConfigurationMap) {
        // Try to get hi-res output sizes
        map.getHighResolutionOutputSizes(internalOutputFormat)
            ?.forEach { sizes.add(it.width, it.height) }
        if (sizes.isEmpty) super.collectPictureSizes(sizes, map)
    }
}