package com.project.myapplication

import android.content.res.AssetFileDescriptor
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class HomeFragment : Fragment() {

    // --- TFLite & Audio Variables ---
    lateinit var tfliteInterpreter: Interpreter
    lateinit var audioRecord: AudioRecord
    val sampleRate = 16000
    val chunkDurationSec = 5
    var isRecording = false
    val moodTimeline = mutableListOf<String>()

    // --- UI Elements ---
    lateinit var dialview: TextView
    lateinit var moodLabel: TextView
    lateinit var suggestion: TextView
    lateinit var d1: Button
    lateinit var d2: Button
    lateinit var d3: Button
    lateinit var d4: Button
    lateinit var d5: Button
    lateinit var d6: Button
    lateinit var d7: Button
    lateinit var d8: Button
    lateinit var d9: Button
    lateinit var ac_dial: Button
    lateinit var d0: Button
    lateinit var backspace: Button
    lateinit var dialnow: Button
    private var dialNumber = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // --- Initialize UI ---
        dialview = view.findViewById(R.id.dialview)
        moodLabel = view.findViewById(R.id.moodLabel)
        suggestion = view.findViewById(R.id.suggestion)
        moodLabel.text = " "
        d1 = view.findViewById(R.id.d1)
        d2 = view.findViewById(R.id.d2)
        d3 = view.findViewById(R.id.d3)
        d4 = view.findViewById(R.id.d4)
        d5 = view.findViewById(R.id.d5)
        d6 = view.findViewById(R.id.d6)
        d7 = view.findViewById(R.id.d7)
        d8 = view.findViewById(R.id.d8)
        d9 = view.findViewById(R.id.d9)
        ac_dial = view.findViewById(R.id.ac_dial)
        d0 = view.findViewById(R.id.d0)
        backspace = view.findViewById(R.id.backspace)
        dialnow = view.findViewById(R.id.dialnow)

        // --- Load TFLite Model ---
        tfliteInterpreter = Interpreter(loadModelFile("model.tflite"))

        // --- Dialer Buttons ---
        val buttons = listOf(d1,d2,d3,d4,d5,d6,d7,d8,d9,d0)
        for ((i, btn) in buttons.withIndex()) {
            btn.setOnClickListener {
                dialNumber += (i + 1).takeIf { i < 9 } ?: 0
                dialview.text = dialNumber
            }
        }
        ac_dial.setOnClickListener { dialNumber = ""; dialview.text = dialNumber }
        backspace.setOnClickListener {
            if (dialNumber.isNotEmpty()) {
                dialNumber = dialNumber.dropLast(1)
                dialview.text = dialNumber
            }
        }

        // --- Start Mood Detection ---
        dialnow.setOnClickListener {
            moodLabel.text = "Mood: Analysing..."
            startMoodDetection()
        }

        return view
    }

    // --- Load Model from Assets ---
    private fun loadModelFile(fileName: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = requireContext().assets.openFd(fileName)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // --- Start Audio Capture & Mood Detection ---
    private fun startMoodDetection() {
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        val chunkSize = sampleRate * chunkDurationSec
        val audioBuffer = ShortArray(chunkSize)
        val floatBuffer = FloatArray(chunkSize)

        isRecording = true
        audioRecord.startRecording()

        Thread {
            while (isRecording) {
                val read = audioRecord.read(audioBuffer, 0, chunkSize)
                for (i in 0 until read) floatBuffer[i] = audioBuffer[i] / 32768.0f

                val mood = predictMood(floatBuffer)
                requireActivity().runOnUiThread { updateMoodTimeline(mood) }

                Thread.sleep(chunkDurationSec * 1000L)
            }
        }.start()
    }

    private fun stopMoodDetection() {
        isRecording = false
        if (::audioRecord.isInitialized) {
            audioRecord.stop()
            audioRecord.release()
        }
    }

    private fun predictMood(audioData: FloatArray): String {
        val input15600 = FloatArray(15600)
        for (i in input15600.indices) {
            input15600[i] = if (i < audioData.size) audioData[i] else 0f
        }

        val input = arrayOf(input15600)
        val output = Array(1) { FloatArray(521) }
        tfliteInterpreter.run(input, output)

        val predictedIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val predictedClass = yamnetClassNames.getOrElse(predictedIndex) { "Unknown" }

        return when (predictedClass) {
            "Laughter", "Cheering", "Applause" -> "Happy"
            "Crying", "Sobbing", "Whimper" -> "Sad"
            "Shout", "Yell" -> "Excited"
            "Gasp" -> "Surprised"
            else -> "Neutral"
        }
    }


    // --- Update UI and Timeline ---
    private fun updateMoodTimeline(mood: String) {
        moodTimeline.add(mood)
        moodLabel.text = "Current Mood: $mood"
    }

    // --- Calculate Final Mood ---
    fun calculateFinalMood(): String {
        val counts = moodTimeline.groupingBy { it }.eachCount()
        return counts.maxByOrNull { it.value }?.key ?: "Neutral"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopMoodDetection()
    }

    companion object {
        val yamnetClassNames = arrayOf(
            "Speech",
            "Child speech, kid speaking",
            "Conversation",
            "Narration, monologue",
            "Babbling",
            "Speech synthesizer",
            "Shout",
            "Bellow",
            "Whoop",
            "Yell",
            "Children shouting",
            "Screaming",
            "Whispering",
            "Laughter",
            "Baby laughter",
            "Giggle",
            "Snicker",
            "Belly laugh",
            "Chuckle, chortle",
            "Crying, sobbing",
            "Baby cry, infant cry",
            "Whimper",
            "Wail, moan",
            "Sigh",
            "Singing",
            "Choir",
            "Yodeling",
            "Chant",
            "Mantra",
            "Child singing",
            "Synthetic singing",
            "Rapping",
            "Humming",
            "Groan",
            "Grunt",
            "Whistling",
            "Breathing",
            "Wheeze",
            "Snoring",
            "Gasp",
            "Pant",
            "Snort",
            "Cough",
            "Throat clearing",
            "Sneeze",
            "Sniff",
            "Run",
            "Shuffle",
            "Walk, footsteps",
            "Chewing, mastication",
            "Biting",
            "Gargling",
            "Stomach rumble",
            "Burping, eructation",
            "Hiccup",
            "Fart",
            "Hands",
            "Finger snapping",
            "Clapping",
            "Heart sounds, heartbeat",
            "Heart murmur",
            "Cheering",
            "Applause",
            "Chatter",
            "Crowd",
            "Hubbub, speech noise, speech babble",
            "Children playing",
            "Animal",
            "Domestic animals, pets",
            "Dog",
            "Bark",
            "Yip",
            "Howl",
            "Bow-wow",
            "Growling",
            "Whimper (dog)",
            "Cat",
            "Purr",
            "Meow",
            "Hiss",
            "Caterwaul",
            "Livestock, farm animals, working animals",
            "Horse",
            "Clip-clop",
            "Neigh, whinny",
            "Cattle, bovinae",
            "Moo",
            "Cowbell",
            "Pig",
            "Oink",
            "Goat",
            "Bleat",
            "Sheep",
            "Fowl",
            "Chicken, rooster",
            "Cluck",
            "Crowing, cock-a-doodle-doo",
            "Turkey",
            "Gobble",
            "Duck",
            "Quack",
            "Goose",
            "Honk",
            "Wild animals",
            "Roaring cats (lions, tigers)",
            "Roar",
            "Bird",
            "Bird vocalization, bird call, bird song",
            "Chirp, tweet",
            "Squawk",
            "Pigeon, dove",
            "Coo",
            "Crow",
            "Caw",
            "Owl",
            "Hoot",
            "Bird flight, flapping wings",
            "Canidae, dogs, wolves",
            "Rodents, rats, mice",
            "Mouse",
            "Patter",
            "Insect",
            "Cricket",
            "Mosquito",
            "Fly, housefly",
            "Buzz",
            "Bee, wasp, etc.",
            "Frog",
            "Croak",
            "Snake",
            "Rattle",
            "Whale vocalization",
            "Music",
            "Musical instrument",
            "Plucked string instrument",
            "Guitar",
            "Electric guitar",
            "Bass guitar",
            "Acoustic guitar",
            "Steel guitar, slide guitar",
            "Tapping (guitar technique)",
            "Strum",
            "Banjo",
            "Sitar",
            "Mandolin",
            "Zither",
            "Ukulele",
            "Keyboard (musical)",
            "Piano",
            "Electric piano",
            "Organ",
            "Electronic organ",
            "Hammond organ",
            "Synthesizer",
            "Sampler",
            "Harpsichord",
            "Percussion",
            "Drum kit",
            "Drum machine",
            "Drum",
            "Snare drum",
            "Rimshot",
            "Drum roll",
            "Bass drum",
            "Timpani",
            "Tabla",
            "Cymbal",
            "Hi-hat",
            "Wood block",
            "Tambourine",
            "Rattle (instrument)",
            "Maraca",
            "Gong",
            "Tubular bells",
            "Mallet percussion",
            "Marimba, xylophone",
            "Glockenspiel",
            "Vibraphone",
            "Steelpan",
            "Orchestra",
            "Brass instrument",
            "French horn",
            "Trumpet",
            "Trombone",
            "Bowed string instrument",
            "String section",
            "Violin, fiddle",
            "Pizzicato",
            "Cello",
            "Double bass",
            "Wind instrument, woodwind instrument",
            "Flute",
            "Saxophone",
            "Clarinet",
            "Harp",
            "Bell",
            "Church bell",
            "Jingle bell",
            "Bicycle bell",
            "Tuning fork",
            "Chime",
            "Wind chime",
            "Change ringing (campanology)",
            "Harmonica",
            "Accordion",
            "Bagpipes",
            "Didgeridoo",
            "Shofar",
            "Theremin",
            "Singing bowl",
            "Scratching (performance technique)",
            "Pop music",
            "Hip hop music",
            "Beatboxing",
            "Rock music",
            "Heavy metal",
            "Punk rock",
            "Grunge",
            "Progressive rock",
            "Rock and roll",
            "Psychedelic rock",
            "Rhythm and blues",
            "Soul music",
            "Reggae",
            "Country",
            "Swing music",
            "Bluegrass",
            "Funk",
            "Folk music",
            "Middle Eastern music",
            "Jazz",
            "Disco",
            "Classical music",
            "Opera",
            "Electronic music",
            "House music",
            "Techno",
            "Dubstep",
            "Drum and bass",
            "Electronica",
            "Electronic dance music",
            "Ambient music",
            "Trance music",
            "Music of Latin America",
            "Salsa music",
            "Flamenco",
            "Blues",
            "Music for children",
            "New-age music",
            "Vocal music",
            "A capella",
            "Music of Africa",
            "Afrobeat",
            "Christian music",
            "Gospel music",
            "Music of Asia",
            "Carnatic music",
            "Music of Bollywood",
            "Ska",
            "Traditional music",
            "Independent music",
            "Song",
            "Background music",
            "Theme music",
            "Jingle (music)",
            "Soundtrack music",
            "Lullaby",
            "Video game music",
            "Christmas music",
            "Dance music",
            "Wedding music",
            "Happy music",
            "Sad music",
            "Tender music",
            "Exciting music",
            "Angry music",
            "Scary music",
            "Wind",
            "Rustling leaves",
            "Wind noise (microphone)",
            "Thunderstorm",
            "Thunder",
            "Water",
            "Rain",
            "Raindrop",
            "Rain on surface",
            "Stream",
            "Waterfall",
            "Ocean",
            "Waves, surf",
            "Steam",
            "Gurgling",
            "Fire",
            "Crackle",
            "Vehicle",
            "Boat, Water vehicle",
            "Sailboat, sailing ship",
            "Rowboat, canoe, kayak",
            "Motorboat, speedboat",
            "Ship",
            "Motor vehicle (road)",
            "Car",
            "Vehicle horn, car horn, honking",
            "Toot",
            "Car alarm",
            "Power windows, electric windows",
            "Skidding",
            "Tire squeal",
            "Car passing by",
            "Race car, auto racing",
            "Truck",
            "Air brake",
            "Air horn, truck horn",
            "Reversing beeps",
            "Ice cream truck, ice cream van",
            "Bus",
            "Emergency vehicle",
            "Police car (siren)",
            "Ambulance (siren)",
            "Fire engine, fire truck (siren)",
            "Motorcycle",
            "Traffic noise, roadway noise",
            "Rail transport",
            "Train",
            "Train whistle",
            "Train horn",
            "Railroad car, train wagon",
            "Train wheels squealing",
            "Subway, metro, underground",
            "Aircraft",
            "Aircraft engine",
            "Jet engine",
            "Propeller, airscrew",
            "Helicopter",
            "Fixed-wing aircraft, airplane",
            "Bicycle",
            "Skateboard",
            "Engine",
            "Light engine (high frequency)",
            "Dental drill, dentist's drill",
            "Lawn mower",
            "Chainsaw",
            "Medium engine (mid frequency)",
            "Heavy engine (low frequency)",
            "Engine knocking",
            "Engine starting",
            "Idling",
            "Accelerating, revving, vroom",
            "Door",
            "Doorbell",
            "Ding-dong",
            "Sliding door",
            "Slam",
            "Knock",
            "Tap",
            "Squeak",
            "Cupboard open or close",
            "Drawer open or close",
            "Dishes, pots, and pans",
            "Cutlery, silverware",
            "Chopping (food)",
            "Frying (food)",
            "Microwave oven",
            "Blender",
            "Water tap, faucet",
            "Sink (filling or washing)",
            "Bathtub (filling or washing)",
            "Hair dryer",
            "Toilet flush",
            "Toothbrush",
            "Electric toothbrush",
            "Vacuum cleaner",
            "Zipper (clothing)",
            "Keys jangling",
            "Coin (dropping)",
            "Scissors",
            "Electric shaver, electric razor",
            "Shuffling cards",
            "Typing",
            "Typewriter",
            "Computer keyboard",
            "Writing",
            "Alarm",
            "Telephone",
            "Telephone bell ringing",
            "Ringtone",
            "Telephone dialing, DTMF",
            "Dial tone",
            "Busy signal",
            "Alarm clock",
            "Siren",
            "Civil defense siren",
            "Buzzer",
            "Smoke detector, smoke alarm",
            "Fire alarm",
            "Foghorn",
            "Whistle",
            "Steam whistle",
            "Mechanisms",
            "Ratchet, pawl",
            "Clock",
            "Tick",
            "Tick-tock",
            "Gears",
            "Pulleys",
            "Sewing machine",
            "Mechanical fan",
            "Air conditioning",
            "Cash register",
            "Printer",
            "Camera",
            "Single-lens reflex camera",
            "Tools",
            "Hammer",
            "Jackhammer",
            "Sawing",
            "Filing (rasp)",
            "Sanding",
            "Power tool",
            "Drill",
            "Explosion",
            "Gunshot, gunfire",
            "Machine gun",
            "Fusillade",
            "Artillery fire",
            "Cap gun",
            "Fireworks",
            "Firecracker",
            "Burst, pop",
            "Eruption",
            "Boom",
            "Wood",
            "Chop",
            "Splinter",
            "Crack",
            "Glass",
            "Chink, clink",
            "Shatter",
            "Liquid",
            "Splash, splatter",
            "Slosh",
            "Squish",
            "Drip",
            "Pour",
            "Trickle, dribble",
            "Gush",
            "Fill (with liquid)",
            "Spray",
            "Pump (liquid)",
            "Stir",
            "Boiling",
            "Sonar",
            "Arrow",
            "Whoosh, swoosh, swish",
            "Thump, thud",
            "Thunk",
            "Electronic tuner",
            "Effects unit",
            "Chorus effect",
            "Basketball bounce",
            "Bang",
            "Slap, smack",
            "Whack, thwack",
            "Smash, crash",
            "Breaking",
            "Bouncing",
            "Whip",
            "Flap",
            "Scratch",
            "Scrape",
            "Rub",
            "Roll",
            "Crushing",
            "Crumpling, crinkling",
            "Tearing",
            "Beep, bleep",
            "Ping",
            "Ding",
            "Clang",
            "Squeal",
            "Creak",
            "Rustle",
            "Whir",
            "Clatter",
            "Sizzle",
            "Clicking",
            "Clickety-clack",
            "Rumble",
            "Plop",
            "Jingle, tinkle",
            "Hum",
            "Zing",
            "Boing",
            "Crunch",
            "Silence",
            "Sine wave",
            "Harmonic",
            "Chirp tone",
            "Sound effect",
            "Pulse",
            "Inside, small room",
            "Inside, large room or hall",
            "Inside, public space",
            "Outside, urban or manmade",
            "Outside, rural or natural",
            "Reverberation",
            "Echo",
            "Noise",
            "Environmental noise",
            "Static",
            "Mains hum",
            "Distortion",
            "Sidetone",
            "Cacophony",
            "White noise",
            "Pink noise",
            "Throbbing",
            "Vibration",
            "Television",
            "Radio",
            "Field recording",
        )

    }
}
