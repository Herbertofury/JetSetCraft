package com.herberto.jetsetcraft.client.crew;

import com.herberto.jetsetcraft.network.JetSetNetwork;
import com.herberto.jetsetcraft.rush.RushRun;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A native, non-pausing crew ledger. Buttons submit intent; displayed eligibility is server-owned. */
final class CrewHouseScreen extends Screen {
    private enum View { HOME, CREW, FACILITIES, JOBS, MEMBER, RECRUIT, SPECIES, FACILITY, PREPARATIONS,
        FOUND, RELOCATE, RENAME, RENAME_MEMBER, ASSIGN, MISSION, JOB, CANCEL_JOB }
    private static final String[] ROLES = {"Courier", "Tagger", "Technician", "DJ"};
    private static final int WHITE = 0xFFE7E7E7, MUTED = 0xFFB8BEC5, CYAN = 0xFF87E1EA, GREEN = 0xFFC5ED55;
    private View view = View.HOME;
    private UUID memberId, jobId;
    private String facilityId = "", missionId = "", nameDraft = "My crew", pendingAction = "", message = "";
    private int page, recruitIndex, roleIndex, textScroll, pendingTicks;
    private boolean pending;
    private EditBox nameField;
    private Button nameSubmit;
    private final List<TextAt> labels = new ArrayList<>();
    private final List<TextAt> detailLines = new ArrayList<>();

    private record TextAt(FormattedCharSequence text, int y, int color) {}

    CrewHouseScreen() { super(Component.literal("Crew House")); message = state().getString("Message"); }

    boolean isView(String expected) { return view.name().equals(expected); }

    void onSnapshot(CompoundTag previous) {
        rememberName();
        CompoundTag current = state();
        message = current.getString("Message");
        boolean changed = current.getLong("Revision") != previous.getLong("Revision")
                || current.getBoolean("Exists") != previous.getBoolean("Exists");
        if (changed && !pendingAction.isEmpty()) {
            view = switch (pendingAction) {
                case "found", "relocate", "rename", "expand", "preparation" -> View.HOME;
                case "recruit" -> View.CREW;
                case "rename_member" -> View.MEMBER;
                case "assign", "claim", "cancel" -> View.JOBS;
                default -> view;
            };
            page = 0;
            textScroll = 0;
        }
        if (!previous.getBoolean("Exists") && current.getBoolean("Exists")) view = View.HOME;
        if (memberId != null && member().isEmpty()) { memberId = null; if (memberView()) view = View.CREW; }
        if (jobId != null && job().isEmpty()) { jobId = null; if (view == View.JOB || view == View.CANCEL_JOB) view = View.JOBS; }
        pending = false;
        pendingTicks = 0;
        pendingAction = "";
        rebuildWidgets();
    }

    @Override
    protected void init() {
        rememberName();
        nameField = null;
        nameSubmit = null;
        labels.clear();
        detailLines.clear();
        int left = left(), span = span(), quarter = (span - 9) / 4;
        View[] tabs = {View.HOME, View.CREW, View.FACILITIES, View.JOBS};
        String[] names = {"Home", "Crew", "Facilities", "Jobs"};
        for (int i = 0; i < tabs.length; i++) {
            View target = tabs[i];
            button(names[i], left + i * (quarter + 3), 43, i == 3 ? span - 3 * (quarter + 3) : quarter,
                    true, "", () -> navigate(target));
        }
        if (!state().getBoolean("Exists") && view != View.HOME && view != View.FOUND) view = View.HOME;
        switch (view) {
            case HOME -> buildHome();
            case CREW, FACILITIES, JOBS, SPECIES, PREPARATIONS, ASSIGN -> buildList();
            case MEMBER -> buildMember();
            case RECRUIT -> buildRecruit();
            case FACILITY -> buildFacility();
            case FOUND, RENAME, RENAME_MEMBER -> buildNameForm();
            case RELOCATE -> buildRelocation();
            case MISSION -> buildMission();
            case JOB, CANCEL_JOB -> buildJob();
        }
        int third = (span - 8) / 3;
        button("Done", left, height - 26, third, true, "Close the ledger and keep playing Minecraft.", this::onClose);
        button("Refresh", left + third + 4, height - 26, third, !pending,
                "Check the server's current home, facilities and mission progress.", () -> send("refresh", "", 0, null));
        boolean inRush = state().getBoolean("InRush");
        button(inRush ? "Return to Minecraft" : "Rival Rush", left + 2 * (third + 4), height - 26,
                span - 2 * (third + 4), inRush || !pending,
                inRush ? "End the current Rush and restore your Minecraft state." : "Open the optional Rush menu; this does not start a run.",
                () -> JetSetNetwork.sendRushAction(inRush ? "leave" : "menu", "", 0, null, 0));
    }

    private void buildHome() {
        CompoundTag s = state();
        if (!s.getBoolean("Exists")) {
            label("Build your own house around a Boombox, then give your crew a place to train and work.", 75, 3, WHITE);
            label(s.getString("CandidateLabel"), 116, 2, CYAN);
            action("Found a home", left(), firstActions(), span(), s.getBoolean("CanFound"), s.getString("FoundReason"),
                    () -> { nameDraft = "My crew"; navigate(View.FOUND); });
            label("Home bounds track facilities without restricting building.", 141, 1, MUTED);
            return;
        }
        label(s.getString("HomeLabel"), 75, 1, WHITE);
        label("Build area: " + s.getInt("Radius") + "-block radius  •  Crew " + list("Members").size() + " / " + s.getInt("Capacity"), 89, 1, CYAN);
        label(xp("Crew", s.getLong("CrewXp"), s.getLong("CrewNextXp")), 103, 1, MUTED);
        button("Preparation: " + preparationName(s.getString("SelectedPreparation")), left(), 119, span(), true,
                "Choose one starting rank for Rival Rush from a physically valid home facility.", () -> navigate(View.PREPARATIONS));
        if (!s.getBoolean("CanManage")) label(s.getString("ManageReason"), 143, 1, 0xFFE6DBA6);
        twoButtons(firstActions(), "Rename", s.getBoolean("CanManage"), s.getString("ManageReason"),
                () -> { nameDraft = s.getString("Name"); navigate(View.RENAME); },
                "Expand — " + s.getInt("ExpandCost") + " supplies", s.getBoolean("CanExpand"), s.getString("ExpandReason"),
                () -> send("expand", "", 0, null));
        twoButtons(secondActions(), "Show boundary", s.getBoolean("CanManage"), s.getString("ManageReason"),
                () -> send("preview", "", 0, null), "Move home", s.getBoolean("CanRelocate"), s.getString("FoundReason"),
                () -> navigate(View.RELOCATE));
    }

    private void buildList() {
        ListTag entries = visibleList();
        int rows = listRows();
        int pages = Math.max(1, (entries.size() + rows - 1) / rows);
        page = Math.max(0, Math.min(page, pages - 1));
        String heading = switch (view) {
            case CREW -> "Crew profiles  •  " + entries.size() + " / " + state().getInt("Capacity");
            case FACILITIES -> "Facilities in your own Minecraft build";
            case JOBS -> "Jobs  •  " + state().getInt("ActiveJobs") + " / " + state().getInt("MissionSlots") + " slots";
            case SPECIES -> "Choose a creature for the new profile";
            case PREPARATIONS -> "One home preparation per Rush";
            case ASSIGN -> "Choose a job for " + member().getString("Name");
            default -> "";
        };
        int headingWidth = view == View.CREW || view == View.JOBS ? span() - 126 : span();
        label(heading, 74, headingWidth, 1, CYAN);
        if (view == View.CREW) button("Recruit profile", left() + span() - 120, 69, 120, !pending,
                "Recruit a persistent roster profile for training and timed jobs.", () -> navigate(View.RECRUIT));
        if (view == View.JOBS) button("Choose member", left() + span() - 120, 69, 120, true,
                "Open a crew member to assign a job.", () -> navigate(View.CREW));
        if (entries.isEmpty()) {
            label(view == View.JOBS ? "No active jobs. Choose a crew member, then send them on a supply run. Jobs progress while the server runs; ready rewards wait for you."
                    : "No entries are available yet. Refresh the ledger after updating your home.", 101, 5, WHITE);
        }
        for (int row = 0; row < rows && page * rows + row < entries.size(); row++) {
            int index = page * rows + row;
            CompoundTag entry = entries.getCompound(index);
            String title = entry.getString("Name"), hint = "";
            boolean enabled = true;
            Runnable click;
            switch (view) {
                case CREW -> {
                    title += "  •  Lv " + entry.getInt("Level") + "  •  " + (entry.getBoolean("Training") ? "Rush training" : entry.getString("Status"));
                    hint = entry.getString("SpeciesName") + " • " + entry.getString("RoleName") + ". " + entry.getString("Status");
                    click = () -> { memberId = id(entry); navigate(View.MEMBER); };
                }
                case FACILITIES -> {
                    title += "  •  " + (entry.getBoolean("Registered")
                            ? "Tier " + entry.getInt("Level") + "  •  " + (entry.getBoolean("Operational") ? "Operational" : "Needs attention")
                            : "Not registered");
                    hint = entry.getString("Status") + "\n" + entry.getString("Requires");
                    click = () -> { facilityId = entry.getString("Id"); navigate(View.FACILITY); };
                }
                case JOBS -> {
                    title = entry.getString("MemberName") + "  •  " + title + "  •  "
                            + (entry.getBoolean("Ready") ? "Ready to claim" : clock(entry.getLong("RemainingTicks")));
                    hint = "Progress is checked when you refresh. Ready rewards remain available until claimed.";
                    click = () -> { jobId = id(entry); navigate(View.JOB); };
                }
                case SPECIES -> { hint = entry.getString("Reason"); click = () -> { recruitIndex = index; navigate(View.RECRUIT); }; }
                case PREPARATIONS -> {
                    if (entry.getString("Id").equals(state().getString("SelectedPreparation"))) title += "  •  Selected";
                    enabled = entry.getBoolean("Available") && !pending;
                    hint = entry.getString("Reason") + " " + perkDescription(entry.getString("Id"));
                    click = () -> send("preparation", entry.getString("Id"), 0, null);
                }
                case ASSIGN -> {
                    title += "  •  " + clock(entry.getLong("DurationTicks")) + "  •  " + entry.getInt("RewardSupplies") + "+ supplies";
                    enabled = entry.getBoolean("CanAssign") && member().getBoolean("CanAssign") && !pending;
                    hint = entry.getString("Reason");
                    click = () -> { missionId = entry.getString("Id"); navigate(View.MISSION); };
                }
                default -> throw new IllegalStateException("Unsupported crew list view " + view);
            }
            button(title, left(), 92 + row * 22, span(), enabled, hint, click);
        }
        int half = (span() - 4) / 2;
        button("‹ Previous", left(), secondActions(), half, page > 0, "", () -> { page--; rebuildWidgets(); });
        button("Next ›", left() + half + 4, secondActions(), span() - half - 4, page + 1 < pages, "", () -> { page++; rebuildWidgets(); });
    }

    private void buildMember() {
        CompoundTag member = member();
        detail(member.getString("Name") + " • " + member.getString("SpeciesName"), GREEN);
        detail(member.getString("RoleName") + " • Level " + member.getInt("Level") + " • " + member.getString("Status"), WHITE);
        detail(xp("Member", member.getLong("Xp"), member.getLong("NextXp")), CYAN);
        detail("Roster profiles support timed jobs and Rush training. Selecting a training focus gives that member experience from eligible Rush results.", MUTED);
        boolean training = member.getBoolean("Training");
        twoButtons(firstActions(), training ? "Stop Rush training" : "Train in Rival Rush",
                training ? state().getBoolean("CanManage") : member.getBoolean("CanTrain"), state().getString("ManageReason"),
                () -> send(training ? "focus_off" : "focus", "", 0, memberId),
                "Rename", state().getBoolean("CanManage"), state().getString("ManageReason"),
                () -> { nameDraft = member.getString("Name"); navigate(View.RENAME_MEMBER); });
        twoButtons(secondActions(), "Choose a job", member.getBoolean("CanAssign"),
                "The member must be idle and a job slot must be available at your home.", () -> navigate(View.ASSIGN),
                "Back to crew", true, "", () -> navigate(View.CREW));
    }

    private void buildRecruit() {
        ListTag recruits = list("Recruits");
        recruitIndex = Math.max(0, Math.min(recruitIndex, Math.max(0, recruits.size() - 1)));
        CompoundTag recruit = recruits.isEmpty() ? new CompoundTag() : recruits.getCompound(recruitIndex);
        int discount = Math.max(0, state().getInt("RecruitDiscount"));
        label("Profiles train and run supply jobs. " + (discount > 0 ? "Home-gang trust saves " + discount + " supplies per recruit."
                : "Earn home-gang reputation for cheaper recruitment."), 75, 2, WHITE);
        button("Creature: " + recruit.getString("Name") + "  •  Change", left(), 105, span(), !recruits.isEmpty(), "", () -> navigate(View.SPECIES));
        button("Role: " + ROLES[roleIndex] + "  •  Change", left(), 129, span(), true,
                "A job's preferred role can improve the supplies awarded at dispatch.", () -> { roleIndex = (roleIndex + 1) % ROLES.length; rebuildWidgets(); });
        action("Recruit — " + recruit.getInt("Cost") + " supplies", left(), firstActions(), span(),
                recruit.getBoolean("CanRecruit"), recruit.getString("Reason"), () -> send("recruit", recruit.getString("Id"), roleIndex, null));
        button("Back to crew", left(), secondActions(), span(), true, "", () -> navigate(View.CREW));
    }

    private void buildFacility() {
        CompoundTag facility = find("Facilities", "Id", facilityId);
        detail(facility.getString("Name") + " • Level " + facility.getInt("Level"), GREEN);
        detail(facility.getString("Status"), CYAN);
        detail("Build requirements: " + facility.getString("Requires"), WHITE);
        detail("Rush preparation: " + facility.getString("PerkName") + ". " + perkDescription(facility.getString("Perk")), WHITE);
        detail("Place the blocks yourself. Use your Crew Charter on the facility's anchor block, then choose Register facility. Only one house preparation applies to a Rush.", MUTED);
        if (!facility.getString("Reason").isBlank()) detail(facility.getString("Reason"), 0xFFE6DBA6);
        String register = "Register facility" + (facility.getInt("Level") == 0 ? " — " + facility.getInt("Cost") + " supplies" : "");
        twoButtons(firstActions(), register, facility.getBoolean("CanRegister"), facility.getString("Reason"),
                () -> send("facility", facilityId, 0, null), "Upgrade — " + facility.getInt("Cost") + " supplies",
                facility.getBoolean("CanUpgrade"), facility.getString("Reason"), () -> send("upgrade", facilityId, 0, null));
        twoButtons(secondActions(), "Choose preparation", true, "", () -> navigate(View.PREPARATIONS),
                "Back to facilities", true, "", () -> navigate(View.FACILITIES));
    }

    private void buildNameForm() {
        boolean founding = view == View.FOUND, memberName = view == View.RENAME_MEMBER;
        label(founding ? "Choose your crew's name" : memberName ? "Rename this crew member" : "Rename your crew", 75, 1, WHITE);
        nameField = new EditBox(font, left(), 93, span(), 20, Component.literal("Name"));
        nameField.setMaxLength(36);
        nameField.setFilter(value -> value.chars().noneMatch(character -> Character.isISOControl(character) || character == '§'));
        nameField.setValue(nameDraft);
        addRenderableWidget(nameField);
        label(founding ? state().getString("CandidateLabel") : "Names can be changed later; member and home progress are preserved.", 121, 2, MUTED);
        boolean allowed = founding ? state().getBoolean("CanFound") : state().getBoolean("CanManage");
        nameSubmit = action(founding ? "Found here" : "Save name", left(), firstActions(), span(), allowed && !nameDraft.isBlank(),
                founding ? state().getString("FoundReason") : state().getString("ManageReason"),
                () -> send(founding ? "found" : memberName ? "rename_member" : "rename", nameDraft.trim(), 0, memberName ? memberId : null));
        nameField.setResponder(value -> { nameDraft = value; if (nameSubmit != null) nameSubmit.active = allowed && !pending && !value.isBlank(); });
        button("Back", left(), secondActions(), span(), true, "", () -> navigate(memberName ? View.MEMBER : View.HOME));
        setInitialFocus(nameField);
    }

    private void buildRelocation() {
        detail("Move your crew home?", GREEN);
        detail("New anchor: " + state().getString("CandidateLabel"), CYAN);
        detail("Your crew, jobs and progression stay with the home record. Your Minecraft buildings stay where you built them; check and register facilities at the new home.", WHITE);
        detail("Use the Charter on the destination Boombox again if this selection expires.", MUTED);
        action("Confirm move", left(), firstActions(), span(), state().getBoolean("CanRelocate"), state().getString("FoundReason"),
                () -> send("relocate", "", 0, null));
        button("Keep current home", left(), secondActions(), span(), true, "", () -> navigate(View.HOME));
    }

    private void buildMission() {
        CompoundTag mission = find("Missions", "Id", missionId);
        detail(mission.getString("Name") + " • " + clock(mission.getLong("DurationTicks")), GREEN);
        detail("Member: " + member().getString("Name") + ". Preferred role: " + mission.getString("RoleName") + ".", WHITE);
        detail("Base rewards: " + mission.getInt("RewardSupplies") + " supplies, " + mission.getInt("MemberXp")
                + " member XP, " + mission.getInt("CrewXp") + " crew XP, " + mission.getInt("Reputation") + " reputation.", CYAN);
        detail("Role, level and operational facilities can add supplies. The exact reward is fixed when you send the member and is shown in Jobs.", MUTED);
        detail("Time advances while the server runs. Ready rewards wait until claimed; the job never repeats automatically.", MUTED);
        action("Send on job", left(), firstActions(), span(), mission.getBoolean("CanAssign") && member().getBoolean("CanAssign"),
                mission.getString("Reason"), () -> send("assign", missionId, 0, memberId));
        button("Choose another job", left(), secondActions(), span(), true, "", () -> navigate(View.ASSIGN));
    }

    private void buildJob() {
        CompoundTag job = job();
        if (view == View.CANCEL_JOB) {
            detail("Cancel " + job.getString("Name") + "?", GREEN);
            detail("This releases " + job.getString("MemberName") + " and discards every reward from this assignment, even if it is already ready to claim.", WHITE);
            detail("The crew member is retained. No supplies, reputation or experience will be awarded.", 0xFFE6DBA6);
            action("Cancel without rewards", left(), firstActions(), span(), job.getBoolean("CanCancel"), state().getString("ManageReason"),
                    () -> send("cancel", "", 0, jobId));
            button("Keep job", left(), secondActions(), span(), true, "", () -> navigate(View.JOB));
            return;
        }
        detail(job.getString("Name") + " • " + job.getString("MemberName"), GREEN);
        detail(job.getBoolean("Ready") ? "Ready to claim" : clock(job.getLong("RemainingTicks")) + " remaining at last refresh", CYAN);
        detail("Rewards: " + job.getInt("Supplies") + " supplies, " + job.getInt("MemberXp") + " member XP, "
                + job.getInt("CrewXp") + " crew XP, " + job.getInt("Reputation") + " reputation.", WHITE);
        detail("Rewards were fixed at dispatch. Refresh to check progress; ready jobs wait until you return to claim them.", MUTED);
        twoButtons(firstActions(), "Claim rewards", job.getBoolean("CanClaim"), state().getString("ManageReason"),
                () -> send("claim", "", 0, jobId), "Cancel job", job.getBoolean("CanCancel"), "Cancelling forfeits all rewards.",
                () -> navigate(View.CANCEL_JOB));
        button("Back to jobs", left(), secondActions(), span(), true, "", () -> navigate(View.JOBS));
    }

    private void send(String action, String value, int number, UUID target) {
        rememberName();
        pending = true;
        pendingTicks = 0;
        pendingAction = action;
        CrewHouseClient.request(action, value, number, target);
        rebuildWidgets();
    }

    private void navigate(View target) { rememberName(); view = target; page = 0; textScroll = 0; rebuildWidgets(); }
    private void rememberName() { if (nameField != null) nameDraft = nameField.getValue(); }
    private boolean memberView() { return view == View.MEMBER || view == View.RENAME_MEMBER || view == View.ASSIGN || view == View.MISSION; }
    private int span() { return Math.min(460, width - 20); }
    private int left() { return (width - span()) / 2; }
    private int firstActions() { return height - 85; }
    private int secondActions() { return height - 61; }
    private int listRows() { return Math.max(1, Math.min(8, (height - 150) / 22)); }
    private int maxScroll() { return Math.max(0, detailLines.size() * 11 - Math.max(1, height - 166)); }
    private static CompoundTag state() { return CrewHouseClient.state(); }
    private static ListTag list(String key) { return CrewHouseClient.list(key); }
    private static UUID id(CompoundTag tag) { return tag.hasUUID("Id") ? tag.getUUID("Id") : null; }
    private CompoundTag member() { return find("Members", memberId); }
    private CompoundTag job() { return find("Jobs", jobId); }

    private static CompoundTag find(String list, UUID id) {
        if (id != null) for (var tag : list(list)) if (tag instanceof CompoundTag row && id.equals(id(row))) return row;
        return new CompoundTag();
    }
    private static CompoundTag find(String list, String key, String value) {
        for (var tag : list(list)) if (tag instanceof CompoundTag row && row.getString(key).equals(value)) return row;
        return new CompoundTag();
    }
    private ListTag visibleList() {
        return list(switch (view) { case CREW -> "Members"; case FACILITIES -> "Facilities"; case JOBS -> "Jobs";
            case SPECIES -> "Recruits"; case PREPARATIONS -> "Preparations"; case ASSIGN -> "Missions"; default -> ""; });
    }
    private static String xp(String subject, long xp, long next) { return subject + " XP: " + xp + (next > 0 ? " / " + next : " • max level"); }
    private static String clock(long ticks) { long seconds = Math.max(0L, ticks) / 20L; return seconds / 60L + ":" + String.format(java.util.Locale.ROOT, "%02d", seconds % 60L); }
    private static String preparationName(String id) { String name = find("Preparations", "Id", id).getString("Name"); return name.isBlank() ? "None" : name; }
    private static String perkDescription(String id) {
        try { return RushRun.Upgrade.valueOf(id).description(); } catch (IllegalArgumentException error) { return ""; }
    }

    private Button button(String text, int x, int y, int width, boolean enabled, String hint, Runnable action) {
        Button button = addRenderableWidget(Button.builder(Component.literal(clipped(text, width - 8)), ignored -> action.run())
                .bounds(x, y, width, 20).build());
        button.active = enabled;
        if (!hint.isBlank() || font.width(text) > width - 8) button.setTooltip(Tooltip.create(Component.literal(text + (hint.isBlank() ? "" : "\n" + hint))));
        return button;
    }
    private Button action(String text, int x, int y, int width, boolean enabled, String hint, Runnable action) {
        return button(text, x, y, width, enabled && !pending, hint, action);
    }
    private void twoButtons(int y, String first, boolean firstEnabled, String firstHint, Runnable firstAction,
                            String second, boolean secondEnabled, String secondHint, Runnable secondAction) {
        int half = (span() - 4) / 2;
        action(first, left(), y, half, firstEnabled, firstHint, firstAction);
        action(second, left() + half + 4, y, span() - half - 4, secondEnabled, secondHint, secondAction);
    }
    private String clipped(String text, int width) { return font.width(text) <= width ? text : font.plainSubstrByWidth(text, Math.max(0, width - font.width("…"))) + "…"; }
    private void label(String text, int y, int lines, int color) { label(text, y, span(), lines, color); }
    private void label(String text, int y, int width, int lines, int color) {
        List<FormattedCharSequence> wrapped = font.split(Component.literal(text), Math.max(30, width));
        for (int i = 0; i < Math.min(lines, wrapped.size()); i++) labels.add(new TextAt(wrapped.get(i), y + i * 11, color));
    }
    private void detail(String text, int color) {
        for (FormattedCharSequence line : font.split(Component.literal(text), span())) detailLines.add(new TextAt(line, 0, color));
        detailLines.add(new TextAt(FormattedCharSequence.EMPTY, 0, color));
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        gui.fill(left() - 6, 4, left() + span() + 6, height - 4, 0xF2181C20);
        String name = state().getBoolean("Exists") ? state().getString("Name") + " • Crew House" : "Crew House";
        gui.drawCenteredString(font, clipped(name, span()), width / 2, 10, GREEN);
        String summary = state().getBoolean("Exists") ? "Crew level " + state().getInt("CrewLevel") + " • " + state().getInt("Supplies")
                + " supplies • " + state().getString("ReputationTier") : "Build a home • train your crew • take on jobs";
        gui.drawCenteredString(font, clipped(summary, span()), width / 2, 26, WHITE);
        for (TextAt line : labels) gui.drawString(font, line.text(), left(), line.y(), line.color(), true);
        textScroll = Math.max(0, Math.min(textScroll, maxScroll()));
        if (!detailLines.isEmpty()) {
            if (maxScroll() > 0) gui.drawString(font, "Scroll for details", left(), 65, MUTED, true);
            gui.enableScissor(left(), 74, left() + span(), height - 91);
            for (int i = 0; i < detailLines.size(); i++) {
                TextAt line = detailLines.get(i);
                gui.drawString(font, line.text(), left(), 74 + i * 11 - textScroll, line.color(), true);
            }
            gui.disableScissor();
        }
        String note = pending ? "Waiting for the server…" : !message.isBlank() ? message
                : maxScroll() > 0 ? "Scroll or Page Up / Down for details" : "";
        if (!note.isEmpty()) gui.drawString(font, clipped(note, span()), left(), height - 36, 0xFFE6DBA6, true);
        super.render(gui, mouseX, mouseY, partialTick);
        if (!note.isEmpty() && mouseX >= left() && mouseX < left() + span() && mouseY >= height - 37 && mouseY < height - 27)
            gui.renderTooltip(font, Component.literal(note), mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        if (pending && ++pendingTicks >= 20) {
            pending = false;
            pendingAction = "";
            message = "No confirmation yet. Refresh before retrying.";
            rebuildWidgets();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!detailLines.isEmpty()) { textScroll = Math.max(0, Math.min(maxScroll(), textScroll - (int) (amount * 22))); return true; }
        ListTag entries = visibleList();
        if (!entries.isEmpty()) {
            int pages = Math.max(1, (entries.size() + listRows() - 1) / listRows());
            page = Math.max(0, Math.min(pages - 1, page - (int) Math.signum(amount)));
            rebuildWidgets(); return true;
        }
        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if ((keyCode == 257 || keyCode == 335) && nameSubmit != null && nameSubmit.active) { nameSubmit.onPress(); return true; }
        if ((keyCode == 266 || keyCode == 267) && !detailLines.isEmpty()) {
            textScroll = Math.max(0, Math.min(maxScroll(), textScroll + (keyCode == 266 ? -55 : 55))); return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
