import org.dreambot.api.Client;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

@ScriptManifest(name = "acc", description = "this is a test script", author = "Mircoc",
        version = 2.7, category = Category.MONEYMAKING,image = "")
public class AccMakerscript extends AbstractScript {

    /**  Variable  **/
    private int lastGameTick;
    private int skillLevel;
    private int prayerLevel;
    private int attackGoal;
    private int strengthGoal;
    private int defenceGoal;
    private int prayerGoal;
    private int rangedGoal;
    private int magicGoal;
    private long time;
    private long times;
    public int TaskIndex = 0;
    public String Task;
    ArrayList<String> Tasks = new ArrayList<>();
    /** Areas Locations **/
    Area GE_AREA = new Area(3159, 3490, 3170, 3485);
    Area MONASTERY_AREA = new Area(3044, 3498, 3059, 3481);
    Area CHICKEN1_AREA = new Area(3227, 3301, 3236, 3287);
    Area CHICKEN2_AREA = new Area(3185, 3289, 3169, 3302);
    Area COW_AREA = new Area(3243, 3298, 3263, 3279);
    Area BARBARIAN_AREA = new Area(3086, 3434, 3073, 3408);
    Area FISHING_AREA = new Area(3236, 3154, 3246, 3143);
    Area MINING_AREA = new Area(3221, 3150, 3231, 3143);

    @Override
    public void onPaint(Graphics2D graphics) {
        super.onPaint(graphics);
        graphics.setFont(new Font("Arial", Font.BOLD, 21));
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        long end = System.currentTimeMillis() - times;
        long timeRemaining = nextTaskChangeTime - elapsedTime;
        long sec = (end/1000);
        df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        graphics.drawString("Run Time: " + df.format(new Date(end)),30,30);
        graphics.drawString("Status: " + skillLevel,30,50);
        graphics.drawString("World: " + Worlds.getCurrent().toString(),30,130);
        graphics.drawString("Task: " + Task,30,70);
        graphics.drawString("Next at: " + df.format(new Date(timeRemaining > 0 ? timeRemaining : 0)), 30, 90);

    }
    @Override
    public void onStart() {
        super.onStart();
        Tasks.add("FightInChicken1");
        Tasks.add("FightInChicken2");
        Tasks.add("FightInMonastery");
        Tasks.add("FightInCow");
        Tasks.add("BuyBeer");
        Tasks.add("Mining");
        Tasks.add("Fishing");
        Tasks.add("Questing");
        Collections.shuffle(Tasks);
        Task = getRandomTask();
    }
    private long nextTaskChangeTime = 0;
    long elapsedTime=0;
    @Override
    public int onLoop() {
        if (Client.getGameTick() == lastGameTick)
            return 0;
        lastGameTick = Client.getGameTick();
        if (!Client.isLoggedIn())
            return 0;
        skillLevel = Skills.getRealLevel(Skill.ATTACK);
        if (skillLevel>=40 && Skills.getRealLevel(Skill.DEFENCE) >= 40)
            return -1;
        if (times == 0)
            times = System.currentTimeMillis();
        if (time == 0) {
            time = System.currentTimeMillis();
            nextTaskChangeTime = generateRandomTime(1, 3); // تولید زمان تصادفی بین 1 و 3 ساعت
            Task = getRandomTask();
        }
        elapsedTime = System.currentTimeMillis() - time;
        if (elapsedTime >= nextTaskChangeTime) {
            newTask();
        }
        executeTask(Task);
        slp();
        return 0;
    }
    void newTask(){
        Task = getRandomTask();
        time = System.currentTimeMillis(); // به‌روزرسانی زمان شروع
        nextTaskChangeTime = generateRandomTime(1, 3); // تولید زمان تصادفی جدید
        Logger.log("New task selected: " + Task);
    }
    private String getRandomTask() {
        Random random = new Random();
        return Tasks.get(random.nextInt(Tasks.size()));
    }
    private long generateRandomTime(int minHours, int maxHours) {
        Random random = new Random();
        int randomMinutes = random.nextInt((maxHours - minHours) * 60 + 1) + (minHours * 60);
        return randomMinutes * 60 * 1000L; // تبدیل دقیقه به میلی‌ثانیه
    }

    private void executeTask(String task) {
        switch (task) {
            case "FightInChicken1":
                Fight(CHICKEN1_AREA,"Chicken");
                break;
            case "FightInChicken2":
                Fight(CHICKEN2_AREA,"Chicken");
                break;
            case "FightInMonastery":
                Fight(MONASTERY_AREA,"Monk");
                break;
            case "FightInCow":
                newTask();
                break;
            case "BuyBeer":
                newTask();
                break;
            case "Mining":
                if (!MINING_AREA.contains(Players.getLocal())) {
                    Walking.walk(MINING_AREA.getCenter());
                    slp();
                    return;
                }
                Mining();
                break;
            case "Fishing":
                if (!FISHING_AREA.contains(Players.getLocal())) {
                    Walking.walk(FISHING_AREA.getCenter());
                    slp();
                    return;
                }
                Fish();
                break;
            case "Questing":
                newTask();
                break;
        }
    }

    public void Mining(){
        if (Players.getLocal().isMoving())
            return;
        if (Players.getLocal().isAnimating())
            return;
        if (Inventory.isFull()){
            DropItems();
            return;
        }
        GameObject rock = GameObjects.closest("Tin rocks");
        if (rock!=null && rock.exists()){
            rock.interact("Mine");
        }
    }
    public void Fish(){
        if (Inventory.isFull()){
            DropItems();
            return;
        }
        if (Players.getLocal().isAnimating()) {
            slp();
        } else {
            NPCs.closest("Fishing spot").interact("Net");
            slp();
        }
    }
    void Fight(Area area ,String npc){
        skillLevel = Skills.getRealLevel(Skill.ATTACK);
        if (skillLevel >=30 ){
            if (Inventory.contains("Adamant scimitar")){
                Inventory.get("Adamant scimitar").interact();
            }
        }
        else if (skillLevel >=20 ){
            if (Inventory.contains("Mithril scimitar")){
                Inventory.get("Mithril scimitar").interact();
            }
        }
        else {
            if (Inventory.contains("Iron scimitar")){
                Inventory.get("Iron scimitar").interact();
            }
        }
        if (Dialogues.inDialogue() && !GrandExchange.isOpen()){
            if (Dialogues.canContinue())
                Keyboard.holdSpace(() -> false,Calculations.random(50,500));
            if (Dialogues.areOptionsAvailable())
                Keyboard.type("1",false);
            slp();
            return;
        }

        if (Players.getLocal().isMoving())
            return;
        if (getLocalHp() < Calculations.random(4,7) && getLocalHp() != -1){
            if (Task.equals("FightInMonastery")) {
                if (NPCs.closest("Abbot Langley") != null) {
                    NPCs.closest("Abbot Langley").interact();
                    return;
                }
            }
        }
        if (Players.getLocal().isInCombat())
            return;
        if (!area.contains(Players.getLocal())){
            Walking.walk(area.getRandomTile());
            return;
        }
        if (getNPC(npc,area) != null) {
            getNPC(npc,area).interact("Attack");
            slp();
            return;
        }
        return;
    }

    void DropItems(){
        for (int i = 0 ; i <28 ; i++){
            if (Inventory.getItemInSlot(i)!=null)
                if (Inventory.getItemInSlot(i).getName().contains("ore")
                        || Inventory.getItemInSlot(i).getName().contains("Raw")
                        || Inventory.getItemInSlot(i).getName().equals("Logs"))
                {
                    Inventory.getItemInSlot(i).interact("Drop");
                    slp();
                }
        }
    }
    public NPC getNPC(String name, Area area){
        List<NPC> npclist = NPCs.all(name);
        Collections.shuffle(npclist);
        for (NPC npc : npclist) {
            if (!npc.isInCombat())
                if (area.contains(npc))
                    return npc;
        }
        return null;
    }
    public int getLocalHp(){
        if (Widgets.get(160,9)!=null)
            if (Widgets.get(160,9).isVisible())
                return Integer.parseInt(Widgets.get(160,9).getText());
        return -1;
    }
    void slp(){
        int x = Calculations.random(1,50000);
        if (x<5){
            Sleep.sleep(10*1000,70*2000);
        }
        else if (x<20){
            Sleep.sleep(5*500,50*200);
        }
        else if (x<70){
            Sleep.sleep(3*200,15*200);
        }
        else {
            Sleep.sleep(160,6*100);
        }
    }
}