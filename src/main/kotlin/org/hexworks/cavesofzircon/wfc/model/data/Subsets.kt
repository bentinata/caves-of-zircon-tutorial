package org.hexworks.cavesofzircon.wfc.model.data

import com.google.gson.annotations.SerializedName

data class Subsets(
        @SerializedName("subset")
        val subset: List<Subset?>?
)