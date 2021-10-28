package com.thewizrd.shared_resources.actions

import android.os.*
import android.util.Log
import androidx.annotation.Keep
import com.thewizrd.shared_resources.utils.JSONParser
import com.thewizrd.shared_resources.utils.Logger

const val ACTION_PERFORMACTION = "SimpleWear.wearsettings.action.PERFORM_ACTION"
const val EXTRA_ACTION_DATA = "SimpleWear.wearsettings.extra.ACTION_DATA"
const val EXTRA_ACTION_ERROR = "SimpleWear.wearsettings.extra.ACTION_ERROR"

@Keep
class RemoteAction : Parcelable {
    var action: Action?
    var resultReceiver: ResultReceiver?

    constructor(action: Action, resultReceiver: ResultReceiver) {
        this.action = action
        this.resultReceiver = resultReceiver.toParcelableReceiver()
    }

    private constructor() {
        action = null
        resultReceiver = null
    }

    private constructor(parcel: Parcel) : this() {
        action = JSONParser.deserializer(parcel.readString(), Action::class.java)
        resultReceiver = parcel.readParcelable(ResultReceiver::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(JSONParser.serializer(action, Action::class.java))
        parcel.writeParcelable(resultReceiver, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RemoteAction> {
        override fun createFromParcel(parcel: Parcel): RemoteAction {
            return RemoteAction(parcel)
        }

        override fun newArray(size: Int): Array<RemoteAction?> {
            return arrayOfNulls(size)
        }
    }
}

fun ResultReceiver.toParcelableReceiver(): ResultReceiver {
    val parcel = Parcel.obtain()
    this.writeToParcel(parcel, 0)
    parcel.setDataPosition(0)

    val result = ResultReceiver.CREATOR.createFromParcel(parcel)
    parcel.recycle()
    return result
}

@Keep
class RemoteActionReceiver(handler: Handler?) : ResultReceiver(handler) {
    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        if (resultData != null) {
            if (resultData.containsKey(EXTRA_ACTION_ERROR)) {
                Logger.writeLine(
                    Log.ERROR,
                    "Error executing remote action; Error: %s",
                    resultData.getString(EXTRA_ACTION_ERROR)
                )
            }
        }
    }
}

fun Action.toRemoteAction(receiver: ResultReceiver): RemoteAction {
    return RemoteAction(this, receiver)
}