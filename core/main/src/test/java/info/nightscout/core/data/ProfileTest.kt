package info.nightscout.core.data

import android.content.Context
import com.google.common.truth.Truth.assertThat
import dagger.android.AndroidInjector
import info.nightscout.core.extensions.pureProfileFromJson
import info.nightscout.core.profile.ProfileSealed
import info.nightscout.interfaces.Config
import info.nightscout.interfaces.plugin.ActivePlugin
import info.nightscout.interfaces.utils.HardLimits
import info.nightscout.shared.interfaces.ResourceHelper
import info.nightscout.shared.sharedPreferences.SP
import info.nightscout.shared.utils.DateUtil
import info.nightscout.shared.utils.DateUtilImpl
import info.nightscout.sharedtests.HardLimitsMock
import info.nightscout.sharedtests.TestBase
import info.nightscout.sharedtests.TestPumpPlugin
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.`when`
import java.util.Calendar

/**
 * Created by mike on 18.03.2018.
 */
class ProfileTest : TestBase() {

    @Mock lateinit var activePluginProvider: ActivePlugin
    @Mock lateinit var rh: ResourceHelper
    @Mock lateinit var context: Context
    @Mock lateinit var config: Config
    @Mock lateinit var sp: SP

    private lateinit var hardLimits: HardLimits
    private lateinit var dateUtil: DateUtil
    private lateinit var testPumpPlugin: TestPumpPlugin

    private var okProfile = "{\"dia\":\"5\",\"carbratio\":[{\"time\":\"00:00\",\"value\":\"30\"}]," +
        "\"sens\":[{\"time\":\"00:00\",\"value\":\"6\"},{\"time\":\"2:00\",\"value\":\"6.2\"}],\"timezone\":\"UTC\",\"basal\":[{\"time\":\"00:00\",\"value\":\"0.1\"}],\"target_low\":[{\"time\":\"00:00\",\"value\":\"5\"}],\"target_high\":[{\"time\":\"00:00\",\"value\":\"5\"}],\"startDate\":\"1970-01-01T00:00:00.000Z\",\"units\":\"mmol\"}"
    private var belowLimitValidProfile =
        "{\"dia\":\"5\",\"carbratio\":[{\"time\":\"00:00\",\"value\":\"30\"}],\"carbs_hr\":\"20\",\"delay\":\"20\",\"sens\":[{\"time\":\"00:00\",\"value\":\"100\"}],\"timezone\":\"UTC\",\"basal\":[{\"time\":\"00:00\",\"value\":\"0.001\"}],\"target_low\":[{\"time\":\"00:00\",\"value\":\"4\"}],\"target_high\":[{\"time\":\"00:00\",\"value\":\"5\"}],\"startDate\":\"1970-01-01T00:00:00.000Z\",\"units\":\"mmol\"}"
    private var notAlignedBasalValidProfile =
        "{\"dia\":\"5\",\"carbratio\":[{\"time\":\"00:00\",\"value\":\"30\"}],\"carbs_hr\":\"20\",\"delay\":\"20\",\"sens\":[{\"time\":\"00:00\",\"value\":\"100\"}],\"timezone\":\"UTC\",\"basal\":[{\"time\":\"00:30\",\"value\":\"0.1\"}],\"target_low\":[{\"time\":\"00:00\",\"value\":\"4\"}],\"target_high\":[{\"time\":\"00:00\",\"value\":\"5\"}],\"startDate\":\"1970-01-01T00:00:00.000Z\",\"units\":\"mmol\"}"
    private var notStartingAtZeroValidProfile =
        "{\"dia\":\"5\",\"carbratio\":[{\"time\":\"00:30\",\"value\":\"30\"}],\"carbs_hr\":\"20\",\"delay\":\"20\",\"sens\":[{\"time\":\"00:00\",\"value\":\"100\"}],\"timezone\":\"UTC\",\"basal\":[{\"time\":\"00:00\",\"value\":\"0.1\"}],\"target_low\":[{\"time\":\"00:00\",\"value\":\"4\"}],\"target_high\":[{\"time\":\"00:00\",\"value\":\"5\"}],\"startDate\":\"1970-01-01T00:00:00.000Z\",\"units\":\"mmol\"}"
    private var noUnitsValidProfile =
        "{\"dia\":\"5\",\"carbratio\":[{\"time\":\"00:00\",\"value\":\"30\"}],\"carbs_hr\":\"20\",\"delay\":\"20\",\"sens\":[{\"time\":\"00:00\",\"value\":\"100\"}],\"timezone\":\"UTC\",\"basal\":[{\"time\":\"00:00\",\"value\":\"0.1\"}],\"target_low\":[{\"time\":\"00:00\",\"value\":\"4\"}],\"target_high\":[{\"time\":\"00:00\",\"value\":\"5\"}],\"startDate\":\"1970-01-01T00:00:00.000Z\"}"
    private var wrongProfile =
        "{\"dia\":\"5\",\"carbs_hr\":\"20\",\"delay\":\"20\",\"sens\":[{\"time\":\"00:00\",\"value\":\"100\"}],\"timezone\":\"UTC\",\"basal\":[{\"time\":\"00:00\",\"value\":\"0.1\"}],\"target_low\":[{\"time\":\"00:00\",\"value\":\"4\"}],\"target_high\":[{\"time\":\"00:00\",\"value\":\"5\"}],\"startDate\":\"1970-01-01T00:00:00.000Z\",\"units\":\"mmol\"}"

    //String profileStore = "{\"defaultProfile\":\"Default\",\"store\":{\"Default\":" + validProfile + "}}";

    @BeforeEach
    fun prepare() {
        testPumpPlugin = TestPumpPlugin { AndroidInjector { } }
        dateUtil = DateUtilImpl(context)
        hardLimits = HardLimitsMock(sp, rh)
        `when`(activePluginProvider.activePump).thenReturn(testPumpPlugin)
        `when`(rh.gs(info.nightscout.core.ui.R.string.profile_per_unit)).thenReturn("/U")
        `when`(rh.gs(info.nightscout.core.ui.R.string.profile_carbs_per_unit)).thenReturn("g/U")
        `when`(rh.gs(info.nightscout.core.ui.R.string.profile_ins_units_per_hour)).thenReturn("U/h")
        `when`(rh.gs(anyInt(), anyString())).thenReturn("")
    }

    @Test
    fun doTests() {

        // Test valid profile
        var p = ProfileSealed.Pure(pureProfileFromJson(JSONObject(okProfile), dateUtil)!!)
        assertThat(p.isValid("Test", testPumpPlugin, config, rh, rxBus, hardLimits, false).isValid).isTrue()
//        assertThat(p.log()).contains("NS units: mmol")
//        JSONAssertions.assertEquals(JSONObject(okProfile), p.toPureNsJson(dateUtil), false)
        assertThat(p.dia).isWithin(0.01).of(5.0)
//       assertThat(p.timeZone).isEqualTo(TimeZone.getTimeZone("UTC"))
        assertThat(dateUtil.formatHHMM(30 * 60)).isEqualTo("00:30")
        val c = Calendar.getInstance()
        c[Calendar.HOUR_OF_DAY] = 1
        c[Calendar.MINUTE] = 0
        c[Calendar.SECOND] = 0
        c[Calendar.MILLISECOND] = 0
        assertThat(p.getIsfMgdl(c.timeInMillis)).isWithin(0.01).of(108.0)
        c[Calendar.HOUR_OF_DAY] = 2
        assertThat(p.getIsfMgdl(c.timeInMillis)).isWithin(0.01).of(111.6)
//        assertThat(p.getIsfTimeFromMidnight(2 * 60 * 60)).isWithin(0.01).of(110.0)
        assertThat(p.getIsfList(rh, dateUtil).replace(".", ",")).isEqualTo(
            """
    00:00    6,0 mmol/U
    02:00    6,2 mmol/U
    """.trimIndent() 
        )
        assertThat(p.getIc(c.timeInMillis)).isWithin(0.01).of(30.0)
        assertThat(p.getIcTimeFromMidnight(2 * 60 * 60)).isWithin(0.01).of(30.0)
        assertThat(p.getIcList(rh, dateUtil).replace(".", ",")).isEqualTo("00:00    30,0 g/U")
        assertThat(p.getBasal(c.timeInMillis)).isWithin(0.01).of(0.1)
        assertThat(p.getBasalTimeFromMidnight(2 * 60 * 60)).isWithin(0.01).of(0.1)
        assertThat(p.getBasalList(rh, dateUtil).replace(".", ",")).isEqualTo("00:00    0,10 U/h")
        assertThat(p.getBasalValues()[0].value).isWithin(0.01).of(0.1)
        assertThat(p.getMaxDailyBasal()).isWithin(0.01).of(0.1)
        assertThat(p.percentageBasalSum()).isWithin(0.01).of(2.4)
        assertThat(p.baseBasalSum()).isWithin(0.01).of(2.4)
//        assertThat( p.getTargetMgdl(2 * 60 * 60)).isWithin(0.01).of(81.0)
        assertThat( p.getTargetLowMgdl(c.timeInMillis)).isWithin(0.01).of(90.0)
//        assertThat( p.getTargetLowTimeFromMidnight(2 * 60 * 60)).isWithin(0.01).of(4.0)
        assertThat( p.getTargetHighMgdl(c.timeInMillis)).isWithin(0.01).of(90.0)
//        assertThat( p.getTargetHighTimeFromMidnight(2 * 60 * 60)).isWithin(0.01).of(5.0)
        assertThat(p.getTargetList(rh, dateUtil).replace(".", ",")).isEqualTo("00:00    5,0 - 5,0 mmol")
        assertThat(p.percentage).isEqualTo(100)
        assertThat(p.timeshift).isEqualTo(0)

        //Test basal profile below limit
        p = ProfileSealed.Pure(pureProfileFromJson(JSONObject(belowLimitValidProfile), dateUtil)!!)
        p.isValid("Test", testPumpPlugin, config, rh, rxBus, hardLimits, false)

        // Test profile w/o units
        assertThat(pureProfileFromJson(JSONObject(noUnitsValidProfile), dateUtil)).isNull()

        //Test profile not starting at midnight
        p = ProfileSealed.Pure(pureProfileFromJson(JSONObject(notStartingAtZeroValidProfile), dateUtil)!!)
        assertThat(p.getIc(0)).isWithin(0.01).of(30.0)

        // Test wrong profile
        assertThat(pureProfileFromJson(JSONObject(wrongProfile), dateUtil)).isNull()

        // Test percentage functionality
        p = ProfileSealed.Pure(pureProfileFromJson(JSONObject(okProfile), dateUtil)!!)
        p.pct = 50
        assertThat(p.getBasal(c.timeInMillis)).isWithin(0.01).of(0.05)
        assertThat(p.percentageBasalSum()).isWithin(0.01).of(1.2)
        assertThat(p.getIc(c.timeInMillis)).isWithin(0.01).of(60.0)
        assertThat(p.getIsfMgdl(c.timeInMillis)).isWithin(0.01).of(223.2)

        // Test timeshift functionality
        p = ProfileSealed.Pure(pureProfileFromJson(JSONObject(okProfile), dateUtil)!!)
        p.ts = 1
        assertThat(p.getIsfList(rh, dateUtil).replace(',', '.')).isEqualTo(
            """
                00:00    6.2 mmol/U
                01:00    6.0 mmol/U
                03:00    6.2 mmol/U
                """.trimIndent()
        )

        // Test hour alignment
        testPumpPlugin.pumpDescription.is30minBasalRatesCapable = false
        p = ProfileSealed.Pure(pureProfileFromJson(JSONObject(notAlignedBasalValidProfile), dateUtil)!!)
        p.isValid("Test", testPumpPlugin, config, rh, rxBus, hardLimits, false)
    }
}