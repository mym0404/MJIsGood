package happy.mjstudio.sopt27.presentation

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint
import happy.mjstudio.sopt27.R
import happy.mjstudio.sopt27.databinding.FragmentDetailBinding
import happy.mjstudio.sopt27.model.Profile
import happy.mjstudio.sopt27.presentation.adapter.ProfileAdapter
import happy.mjstudio.sopt27.utils.AutoClearedValue

@AndroidEntryPoint
class DetailFragment : Fragment() {

    private var mBinding: FragmentDetailBinding by AutoClearedValue()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        FragmentDetailBinding.inflate(inflater, container, false).let {
            mBinding = it
            it.root
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBinding.lifecycleOwner = viewLifecycleOwner

        setTransition()
        configureList()
    }

    private fun setTransition() {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            duration = 500L
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(requireContext().getColor(R.color.colorTransparent))
        }
    }

    private fun configureList() = mBinding.list.run {
        adapter = ProfileAdapter().apply {
            submitItems((1..100).map {
                Profile("Title$it", "sub title - $it")
            })
        }
    }
}