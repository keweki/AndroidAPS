package info.nightscout.androidaps.plugins.constraints.objectives.objectives;

import java.util.List;

import info.nightscout.androidaps.R;

public class Objective2 extends Objective {


    public Objective2() {
        super("exam", R.string.objectives_exam_objective, R.string.objectives_exam_gate);
    }

    @Override
    protected void setupTasks(List<Task> tasks) {
        tasks.add(new ExamTask(R.string.dia_meaningofdia, R.string.dia_whatmeansdia,"dia")
                .option(new Option(R.string.dia_minimumis3h, false))
                .option(new Option(R.string.dia_minimumis5h, true))
                .option(new Option(R.string.dia_meaningisequaltodiapump, false))
                .option(new Option(R.string.dia_valuemustbedetermined, true))
                .hint(new Hint(R.string.dia_hint1))
        );
        tasks.add(new ExamTask(R.string.hypott, R.string.hypott_whenhypott,"hypott")
                .option(new Option(R.string.hypott_goinglow, true))
                .option(new Option(R.string.hypott_havinglow, true))
                .option(new Option(R.string.hypott_notlowanymorebutrising, false))
                .option(new Option(R.string.hypott_havehadalowbg, false))
                .hint(new Hint(R.string.hypott_hint1))
        );
        tasks.add(new ExamTask(R.string.offlineprofile, R.string.offlineprofile_whatprofile,"offlineprofile")
                .option(new Option(R.string.localprofile, true))
                .option(new Option(R.string.nsprofile, false))
                .option(new Option(R.string.offlineprofile_nsprofile, true))
                .hint(new Hint(R.string.offlineprofile_hint1))
        );
     }
}
