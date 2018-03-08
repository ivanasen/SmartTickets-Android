package com.ivanasen.smarttickets.util

import android.net.Uri
import android.os.ParcelFileDescriptor
import android.support.v4.content.FileProvider

class GenericFileProvider: FileProvider() {

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        return super.openFile(uri, mode)
    }
}