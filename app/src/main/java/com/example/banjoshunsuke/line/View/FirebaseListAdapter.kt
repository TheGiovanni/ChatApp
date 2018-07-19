package com.example.banjoshunsuke.line.View

import android.view.ViewGroup
import android.text.method.TextKeyListener.clear
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.BaseAdapter
import com.firebase.client.ChildEventListener
import com.firebase.client.DataSnapshot
import com.firebase.client.FirebaseError
import com.firebase.client.Query


public abstract class FirebaseListAdapter<T>

(private val mRef: Query, private val mModelClass: Class<T>, private val mLayout: Int, activity: Activity) : BaseAdapter() {
    private val mInflater: LayoutInflater
    private val mModels: MutableList<T>
    private val mKeys: MutableList<String>
    private val mListener: ChildEventListener


    init {
        mInflater = activity.layoutInflater
        mModels = ArrayList()
        mKeys = ArrayList()
        // Look for all child events. We will then map them to our own internal ArrayList, which backs ListView
        mListener = this.mRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {

                val model = dataSnapshot.getValue(this@FirebaseListAdapter.mModelClass)
                val key = dataSnapshot.getKey()

                // Insert into the correct location, based on previousChildName
                if (previousChildName == null) {
                    mModels.add(0, model)
                    mKeys.add(0, key)
                } else {
                    val previousIndex = mKeys.indexOf(previousChildName)
                    val nextIndex = previousIndex + 1
                    if (nextIndex == mModels.size) {
                        mModels.add(model)
                        mKeys.add(key)
                    } else {
                        mModels.add(nextIndex, model)
                        mKeys.add(nextIndex, key)
                    }
                }

                notifyDataSetChanged()
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {
                // One of the mModels changed. Replace it in our list and name mapping
                val key = dataSnapshot.getKey()
                val newModel = dataSnapshot.getValue(this@FirebaseListAdapter.mModelClass)
                val index = mKeys.indexOf(key)

                mModels[index] = newModel

                notifyDataSetChanged()
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

                // A model was removed from the list. Remove it from our list and the name mapping
                val key = dataSnapshot.getKey()
                val index = mKeys.indexOf(key)

                mKeys.removeAt(index)
                mModels.removeAt(index)

                notifyDataSetChanged()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {

                // A model changed position in the list. Update our list accordingly
                val key = dataSnapshot.getKey()
                val newModel = dataSnapshot.getValue(this@FirebaseListAdapter.mModelClass)
                val index = mKeys.indexOf(key)
                mModels.removeAt(index)
                mKeys.removeAt(index)
                if (previousChildName == null) {
                    mModels.add(0, newModel)
                    mKeys.add(0, key)
                } else {
                    val previousIndex = mKeys.indexOf(previousChildName)
                    val nextIndex = previousIndex + 1
                    if (nextIndex == mModels.size) {
                        mModels.add(newModel)
                        mKeys.add(key)
                    } else {
                        mModels.add(nextIndex, newModel)
                        mKeys.add(nextIndex, key)
                    }
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(firebaseError: FirebaseError) {
                Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur")
            }

        })
    }

    fun cleanup() {
        // We're being destroyed, let go of our mListener and forget about all of the mModels
        mRef.removeEventListener(mListener)
        mModels.clear()
        mKeys.clear()
    }

    override fun getCount(): Int {
        return mModels.size
    }

    override fun getItem(i: Int): T {
        return mModels[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getView(i: Int, view: View, viewGroup: ViewGroup): View {
        var view = view
        if (view == null) {
            view = mInflater.inflate(mLayout, viewGroup, false)
        }

        val model = mModels[i]
        // Call out to subclass to marshall this model into the provided view
        populateView(view, model)
        return view
    }

    /**
     * Each time the data at the given Firebase location changes, this method will be called for each item that needs
     * to be displayed. The arguments correspond to the mLayout and mModelClass given to the constructor of this class.
     *
     *
     * Your implementation should populate the view using the data contained in the model.
     *
     * @param v     The view to populate
     * @param model The object containing the data used to populate the view
     */
    protected abstract fun populateView(v: View, model: T)
}