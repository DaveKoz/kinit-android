package org.kinecosystem.kinit.view.adapter

import android.content.Context
import android.databinding.Observable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.kinecosystem.kinit.R
import org.kinecosystem.kinit.model.spend.Offer
import org.kinecosystem.kinit.model.spend.isP2p
import org.kinecosystem.kinit.util.ImageUtils
import org.kinecosystem.kinit.viewmodel.spend.SpendViewModel

class OfferListAdapter(private val context: Context, private val model: SpendViewModel) : RecyclerView.Adapter<OfferListAdapter.ViewHolder>() {


    private var offerList: List<Offer> = model.offers()
    val propertyChangeCallBack = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            refresh()
        }
    }

    fun refresh() {
        offerList = model.offers()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): OfferListAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.offer_card, parent, false)
        return ViewHolder(context, view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(offerList[position])
        holder.view.setOnClickListener { view -> model.onItemClicked(position) }
    }

    override fun getItemCount(): Int {
        return offerList.size
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        model.offersRepository.offers.removeOnPropertyChangedCallback(propertyChangeCallBack)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        model.offersRepository.offers.addOnPropertyChangedCallback(propertyChangeCallBack)
    }


    class ViewHolder(private val context: Context, val view: View) : RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.offer_title)
        private val providerName: TextView = view.findViewById(R.id.provider_name)
        private val reward: TextView = view.findViewById(R.id.offer_reward)
        private val imageView: ImageView = view.findViewById(R.id.offer_image)
        private val logoView: ImageView = view.findViewById(R.id.offer_logo)

        fun bind(offer: Offer) {
            title.text = offer.title
            providerName.text = offer.provider!!.name
            if (offer.isP2p()) {
                reward.visibility = View.INVISIBLE
            } else {
                reward.text = offer.price!!.toString() + " KIN"
            }
            ImageUtils.loadImageIntoView(context, offer.imageUrl, imageView)
            ImageUtils.loadImageIntoView(context, offer.provider.imageUrl, logoView)
        }
    }
}