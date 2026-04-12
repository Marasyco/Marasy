package com.blueray.marasy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blueray.marasy.R
import com.blueray.marasy.databinding.ActivitySectorsBinding
import com.blueray.marasy.helpers.HelperUtils.showErrorToast
import com.blueray.marasy.model.NetworkResults
import com.blueray.marasy.model.SectorsData
import com.blueray.marasy.viewmodel.AppViewModel

class SectorsActivity : BaseActivity() {

    private lateinit var binding: ActivitySectorsBinding
    private val viewmodel by viewModels<AppViewModel>()

    private val sectorList: MutableList<SectorsData> = mutableListOf()
    private val subSectorList: MutableList<SectorsData> = mutableListOf()

    private var selectedSectorId: String? = null
    private var selectedSubSectorId: String? = null

    // Final id you want to pass onward (sector or sub-sector depending on selection)
    private var sectorIdForSubmit: String = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySectorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start with sub-sector disabled until sector is picked & data is loaded
        binding.subSectore.isEnabled = false

        // Load top-level sectors
        viewmodel.retrieveSectors()
        observeSectors()
        observeSubSectors()

        // Sector spinner listener
        binding.sector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val sector = sectorList.getOrNull(position) ?: return
                selectedSectorId = sector.tid
                sectorIdForSubmit = sector.tid

                // Reset sub-sector UI while loading
                binding.subSectore.isEnabled = false
                subSectorList.clear()
                binding.subSectore.adapter = ArrayAdapter(
                    this@SectorsActivity,
                    R.layout.spinner_item,
                    emptyList<String>()
                ).apply { setDropDownViewResource(R.layout.spinner_dropdown_item) }

                // Fetch sub-sectors for this sector
                viewmodel.retrieveSubSectors(sector.tid)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // no-op
            }
        }

        // Sub-sector spinner listener
        binding.subSectore.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val sub = subSectorList.getOrNull(position) ?: return
                selectedSubSectorId = sub.tid

                // If 1000 means "Other", keep sectorIdForSubmit as sector
                sectorIdForSubmit = if (sub.tid == "1000") {
                    selectedSectorId ?: "0"
                } else {
                    sub.tid
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Keep sectorIdForSubmit as sector if no sub-sector picked
                sectorIdForSubmit = selectedSectorId ?: "0"
            }
        }

        // Continue button (single listener)
        binding.continueBtn.setOnClickListener {
            if (sectorIdForSubmit == "0") {
                showErrorToast(this, getString(R.string.fealidsRequred))
                return@setOnClickListener
            }
            val intent = Intent(this, RegisterLocationActivity::class.java)
            RegisterLocationActivity.SECTOR = sectorIdForSubmit
            // putExtra("sectorId", sectorIdForSubmit) // if you need to pass it
            startActivity(intent)
        }
    }

    private fun observeSectors() {
        viewmodel.getSectors().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    sectorList.clear()
                    sectorList.addAll(result.data.data ?: emptyList())

                    val names = sectorList.map { it.name }
                    val adapter = ArrayAdapter(this, R.layout.spinner_item, names)
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                    binding.sector.adapter = adapter

                    // Default selection
                    if (sectorList.isNotEmpty()) {
                        binding.sector.setSelection(0)
                        selectedSectorId = sectorList[0].tid
                        sectorIdForSubmit = selectedSectorId ?: "0"
                        // Trigger initial sub-sector load
                        viewmodel.retrieveSubSectors(selectedSectorId ?: "0")
                    }
                }

                is NetworkResults.Error -> {
                    Toast.makeText(this, result.exception.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }

                else -> Unit
            }
        }
    }

    private fun observeSubSectors() {
        viewmodel.getSubSectors().observe(this) { result ->
            when (result) {
                is NetworkResults.Success -> {
                    subSectorList.clear()
                    subSectorList.addAll(result.data.data ?: emptyList())

                    val names = subSectorList.map { it.name }
                    val adapter = ArrayAdapter(this, R.layout.spinner_item, names)
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

                    // 🔧 FIX: set adapter on the *sub-sector* spinner (not sector)
                    binding.subSectore.adapter = adapter

                    // Enable only if there is at least one real choice
                    binding.subSectore.isEnabled = subSectorList.isNotEmpty()

                    // Default sub-sector selection (optional)
                    if (subSectorList.isNotEmpty()) {
                        binding.subSectore.setSelection(0)
                        selectedSubSectorId = subSectorList[0].tid
                        sectorIdForSubmit = if (selectedSubSectorId == "1000") {
                            selectedSectorId ?: "0"
                        } else {
                            selectedSubSectorId ?: (selectedSectorId ?: "0")
                        }
                    } else {
                        // No sub-sectors -> use sector id
                        selectedSubSectorId = null
                        sectorIdForSubmit = selectedSectorId ?: "0"
                    }
                }

                is NetworkResults.Error -> {
                    Toast.makeText(this, result.exception.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                    // On error, keep using sector id
                    sectorIdForSubmit = selectedSectorId ?: "0"
                    binding.subSectore.isEnabled = false
                }

                else -> Unit
            }
        }
    }
}
