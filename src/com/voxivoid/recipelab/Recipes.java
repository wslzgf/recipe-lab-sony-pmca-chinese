package com.voxivoid.recipelab;

/**
 * Film-look approximations built ONLY from settings the A6000 can store persistently. Names follow the popular
 * film-simulation lists; values are original. No gamma / per-hue colour depth / split-toning exists on this body,
 * so log profiles (S-Log, V-Log, Blackmagic Film) and tinted monochromes (selenium, cyanotype) are not included.
 *
 * style   : Creative Style (stored enum verified on camera: 1 standard, 2 vivid, 3 neutral, 6 mono, 14 sepia; 13 is some style we have not identified, 4..12 still guessed from the runtime list order)
 * sat/con/sharp : Creative Style adjustments (menu range -3..+3; beyond = experimental, camera core accepts sat ±16)
 * matrix  : 1 = PP3 alternate colour matrix (~+45% chroma, blue/green cross-talk)
 * wbMode  : 0 = leave WB as is · 1 = auto · 14 = colour temperature (kelvin)
 * ab / gm : WB fine tune  amber(+)/blue(-)  green(+)/magenta(-)  (-7..+7)
 * pe      : Picture Effect (persistent; when on, Creative Style is ignored by the camera and RAW is disabled)
 * ev      : exposure bias in 1/3 EV steps (persistent)
 * dro     : DRO 0 off, 1..5, 6 auto (persistent)
 * sub     : effect sub-parameter — Soft High-key tint 0 blue / 1 pink / 2 green, Toy tone 0..4, Partial hue 0..3, Posterization 0 colour / 1 b&w
 */
public class Recipes {
    public static class Recipe {
        public final int group; public final String name; public final int style, sat, con, sharp, matrix, wbMode, kelvin, ab, gm, pe, ev, dro, sub;
        Recipe(int group, String name, int style, int sat, int con, int sharp, int matrix, int wbMode, int kelvin, int ab, int gm) {
            this(group, name, style, sat, con, sharp, matrix, wbMode, kelvin, ab, gm, 0, 0, DRO_AUTO);
        }
        Recipe(int group, String name, int style, int sat, int con, int sharp, int matrix, int wbMode, int kelvin, int ab, int gm, int pe, int ev, int dro) {
            this(group, name, style, sat, con, sharp, matrix, wbMode, kelvin, ab, gm, pe, ev, dro, 0);
        }
        Recipe(int group, String name, int style, int sat, int con, int sharp, int matrix, int wbMode, int kelvin, int ab, int gm, int pe, int ev, int dro, int sub) {
            this.sub = sub;
            this.group = group; this.name = name; this.style = style; this.sat = sat; this.con = con; this.sharp = sharp; this.matrix = matrix;
            this.wbMode = wbMode; this.kelvin = kelvin; this.ab = ab; this.gm = gm; this.pe = pe; this.ev = ev; this.dro = dro;
        }
        public boolean isEffect() { return pe != 0; }
        /** one-line summary for lists: "Neutral  -4/-1  A1" */
        public String summary() {
            StringBuilder s = new StringBuilder();
            if (pe != 0) { s.append(PE_LABEL[pe]); String sl = subLabel(pe, sub); if (sl != null) s.append(' ').append(sl); } else s.append(STYLE_LABEL[style]).append("  ").append(sat > 0 ? "+" : "").append(sat).append('/').append(con > 0 ? "+" : "").append(con);
            if (matrix == 1 && pe == 0) s.append("  MTX");
            if (ev != 0) s.append("  ").append(evLabel(ev));
            if (dro != DRO_AUTO) s.append("  DRO ").append(droLabel(dro));
            if (wbMode == 14) s.append("  ").append(kelvin).append('K');
            if (ab != 0) s.append("  ").append(ab > 0 ? "A" + ab : "B" + (-ab));
            if (gm != 0) s.append("  ").append(gm > 0 ? "G" + gm : "M" + (-gm));
            return s.toString();
        }
    }

    public static final int STD = 1, VIVID = 2, NEUTRAL = 3, PORTRAIT = 4, LANDSCAPE = 5, MONO = 6, CLEAR = 7, DEEP = 8, LIGHT = 9, SUNSET = 10, NIGHT = 11, AUTUMN = 12, SEPIA = 14;
    /** index = stored enum; value = runtime color-mode name (API); null = an enum value the camera uses for something we have not identified */
    public static final String[] STYLE_NAMES = { null, "standard", "vivid", "neutral", "portrait", "landscape", "mono", "clear", "deep", "light", "sunset", "night", "red-leaves", null, "sepia" };
    public static final String[] STYLE_LABEL = { "?", "标准", "鲜艳", "中性", "人像", "风景", "黑白", "清澈", "深邃", "明亮", "黄昏", "夜景", "红叶", "?", "棕褐色" };

    private static final int AUTO = 1, K = 14;

    /** Picture Effect: stored byte = index in the runtime list (verified) */
    public static final String[] PE_KEYS = { "off", "toy-camera", "pop-color", "posterization", "retro-photo", "soft-high-key", "part-color", "rough-mono", "soft-focus", "hdr-art", "richtone-mono", "miniature", "illust", "watercolor" };
    public static final String[] PE_LABEL = { "关", "玩具相机", "流行色彩", "色调分离", "复古照片", "柔光亮调", "局部彩色", "高对比度单色", "柔焦", "HDR绘画", "丰富色调黑白", "微缩景观", "插画", "水彩" };
    public static final int PE_OFF = 0, PE_TOY = 1, PE_POP = 2, PE_RETRO = 4, PE_HIGHKEY = 5, PE_HCMONO = 7;
    /** a style whose runtime name we know — the others are enum values seen in the store but never identified */
    public static boolean styleKnown(int v) { return v >= 1 && v < STYLE_NAMES.length && STYLE_NAMES[v] != null; }
    /** chip / HUD labels with a "?n" fallback for an unidentified or out-of-table stored value */
    public static String styleLabel(int v) { return styleKnown(v) ? STYLE_LABEL[v] : "?" + v; }
    public static String peLabel(int v) { return v >= 0 && v < PE_LABEL.length ? PE_LABEL[v] : "?" + v; }
    /** effect sub-parameter (tint / tone / hue / mode): runtime key, stored slot, value names — index = stored byte */
    public static String subKey(int pe) { switch (pe) { case 5: return "pe-soft-high-key-effect"; case 1: return "pe-toy-camera-effect"; case 6: return "pe-part-color-effect"; case 3: return "pe-posterization-effect"; default: return null; } }
    public static int subId(int pe) { switch (pe) { case 5: return 0x010709d8; case 1: return 0x010706f3; case 6: return 0x010706ee; case 3: return 0x010706ef; default: return 0; } }
    public static String[] subValues(int pe) {
        switch (pe) {
            case 5: return new String[] { "blue", "pink", "green" };
            case 1: return new String[] { "normal", "cool", "warm", "green", "magenta" };
            case 6: return new String[] { "red", "green", "blue", "yellow" };
            case 3: return new String[] { "posterization-color", "posterization-bw" };
            default: return null;
        }
    }
    public static String subLabel(int pe, int sub) {
        String[] v = subValues(pe); if (v == null) return null; if (sub < 0 || sub >= v.length) return "?" + sub;
        String key = v[sub];
        if ("blue".equals(key)) return "蓝";
        if ("pink".equals(key)) return "粉";
        if ("green".equals(key)) return "绿";
        if ("normal".equals(key)) return "标准";
        if ("cool".equals(key)) return "冷";
        if ("warm".equals(key)) return "暖";
        if ("magenta".equals(key)) return "品红";
        if ("red".equals(key)) return "红";
        if ("yellow".equals(key)) return "黄";
        if ("posterization-color".equals(key)) return "彩色";
        if ("posterization-bw".equals(key)) return "黑白";
        return key;
    }
    /** DRO: 0 off, 1..5 level, 6 auto */
    public static final int DRO_OFF = 0, DRO_AUTO = 6;
    public static String droLabel(int v) { return v == DRO_AUTO ? "自动" : v == 0 ? "关" : "Lv" + v; }
    /** exposure bias in 1/3 EV steps -> "+0.7" */
    public static String evLabel(int ev) {
        if (ev == 0) return "0";
        int a = Math.abs(ev); String frac = a % 3 == 0 ? ".0" : a % 3 == 1 ? ".3" : ".7";
        return (ev > 0 ? "+" : "-") + (a / 3) + frac;
    }

    // ---- groups (brands) — recipes below MUST be listed in group order
    public static final String[] GROUPS = { "索尼", "富士模拟", "富士胶片", "柯达", "电影卷", "理光GR", "徕卡", "哈苏", "佳能/尼康", "松下/奥巴", "其他胶卷", "伊尔福" };
    private static final int SONY = 0, FSIM = 1, FFILM = 2, KODAK = 3, CINE = 4, RICOH = 5, LEICA = 6, HASSEL = 7, CANIK = 8, PANOLY = 9, OTHER = 10, ILFORD = 11;

    public static final Recipe[] ALL = {
        // ---- Sony (Creative Looks from newer bodies — same pipeline, best fidelity)
        new Recipe(SONY,  "出厂设置（标准）",                        STD,      0,  0,  0, 0, AUTO, 0,     0,  0),
        new Recipe(SONY,  "索尼 PT（人像）",                  PORTRAIT, 0,  0,  0, 0, AUTO, 0,     0,  0),
        new Recipe(SONY,  "索尼 NT（中性）",                   NEUTRAL,  0,  0,  0, 0, AUTO, 0,     0,  0),
        new Recipe(SONY,  "索尼 VV（鲜艳）",                     VIVID,    0,  0,  0, 0, AUTO, 0,     0,  0),
        new Recipe(SONY,  "索尼 VV2",                            VIVID,    2,  1,  0, 1, AUTO, 0,     0,  0),
        new Recipe(SONY,  "索尼 FL（胶片感）",                 NEUTRAL, -4, -1,  0, 0, AUTO, 0,     1,  0),
        new Recipe(SONY,  "索尼 IN（拍立得）",                   NEUTRAL, -4, -3,  0, 0, AUTO, 0,     0, -1),   // -6 is already grey on this body
        new Recipe(SONY,  "索尼 SH（柔和高调）",             LIGHT,   -2, -2,  0, 0, AUTO, 0,     1,  0,  5,  3, 6),
        // ---- Fujifilm simulations
        new Recipe(FSIM,  "富士 Provia",                              STD,      1,  0,  0, 0, AUTO, 0,     0,  0,  0,  0, 6),
        new Recipe(FSIM,  "富士 Velvia",                              VIVID,    5,  2,  0, 1, AUTO, 0,     0,  0),
        new Recipe(FSIM,  "富士 Astia",                               PORTRAIT, 0, -1,  0, 0, AUTO, 0,     1,  0,  0,  1, 6),
        new Recipe(FSIM,  "经典正片",                      STD,     -1,  2,  0, 0, AUTO, 0,     3,  1,  0, -1, 6),   // golden with muted blues; Neutral -3 A2 came out grey-beige
        new Recipe(FSIM,  "经典负片",                    STD,     -3,  3,  1, 0, AUTO, 0,    -1,  1,  0,  0, 6),
        new Recipe(FSIM,  "怀旧负片",                       STD,      1,  0,  0, 0, AUTO, 0,     3,  0,  0,  0, 6),   // amber and saturated; Retro Photo washed it out
        new Recipe(FSIM,  "富士 Reala Ace",                           STD,      0,  1,  0, 0, AUTO, 0,     0,  0),
        new Recipe(FSIM,  "Pro Neg 标准",                         PORTRAIT,-2, -1,  0, 0, AUTO, 0,     0,  0),
        new Recipe(FSIM,  "Pro Neg 高亮",                          PORTRAIT,-2,  1,  0, 0, AUTO, 0,     0,  0),
        new Recipe(FSIM,  "Eterna 影院",                              NEUTRAL, -4, -2, -1, 0, AUTO, 0,     0,  0,  0, -1, 3),   // -6 is already grey on this body
        new Recipe(FSIM,  "Eterna 漂白效果",                NEUTRAL, -6,  3,  0, 0, AUTO, 0,     0,  0,  0, -1, 6),   // sat -8 and below is pure grey on this body
        new Recipe(FSIM,  "Acros 黑白",                               MONO,     0,  1,  1, 0, AUTO, 0,     0,  0),
        new Recipe(FSIM,  "Acros +黄镜",           MONO,     0,  1,  1, 0, K,    4000,  0,  0),
        new Recipe(FSIM,  "Acros +红镜",               MONO,     0,  0,  0, 0, K,    2500,  0,  0,  7,  0, 6),   // HC Mono: deep blacks, dramatic sky
        new Recipe(FSIM,  "Acros +绿镜",             MONO,     0,  1,  1, 0, K,    5600,  0,  4),
        new Recipe(FSIM,  "棕褐",                               SEPIA,    0,  0,  0, 0, AUTO, 0,     0,  0),
        // ---- Fujifilm film stocks
        new Recipe(FFILM, "富士 Pro 400H",                       LIGHT,   -2, -2,  0, 0, AUTO, 0,    -1,  1,  0,  2, 6),   // cool pastel; the high-key green tint was a green wash
        new Recipe(FFILM, "富士 Fortia 50",                      VIVID,    4,  2,  0, 1, AUTO, 0,    -1,  0,  0, -1, 6),   // M1 on top of the matrix went magenta
        new Recipe(FFILM, "富士 Superia 400",                    STD,      1,  1,  0, 0, AUTO, 0,     1,  1,  0,  1, 6),
        new Recipe(FFILM, "富士 C200",                           STD,      0,  0,  0, 0, AUTO, 0,    -1,  1),
        new Recipe(FFILM, "富士 Natura 1600",                    PORTRAIT,-2, -2,  0, 0, AUTO, 0,     1,  0,  0,  1, 6),
        // ---- Kodak
        new Recipe(KODAK, "柯达炮塔160",                    PORTRAIT,-3, -2,  0, 0, AUTO, 0,     1, -1,  0,  2, 6),   // warm pastel with a touch of magenta
        new Recipe(KODAK, "柯达炮塔400",                    PORTRAIT,-1, -1,  0, 0, AUTO, 0,     3,  1,  0,  2, 6),   // golden, soft
        new Recipe(KODAK, "柯达炮塔800",                    PORTRAIT,-2, -2,  0, 0, AUTO, 0,     1,  1,  0,  1, 6),   // matte, faded warm
        new Recipe(KODAK, "柯达金200",                      STD,      2,  1,  0, 0, AUTO, 0,     3,  1,  0,  1, 6),
        new Recipe(KODAK, "柯达全能400",                 STD,      0,  0,  0, 0, AUTO, 0,     0,  1,  0,  1, 6),
        new Recipe(KODAK, "柯达 ColorPlus 200",                STD,      1,  1,  0, 0, AUTO, 0,     2,  1,  0,  1, 6),
        new Recipe(KODAK, "柯达 Ektar 100",                     STD,      3,  1,  1, 0, AUTO, 0,     0,  0,  0, -1, 6),
        new Recipe(KODAK, "柯达 Ektachrome E100",               CLEAR,    3,  1,  0, 0, AUTO, 0,    -1,  0,  0, -1, 6),
        new Recipe(KODAK, "柯达克罗姆64",                       DEEP,     3,  2,  1, 0, AUTO, 0,    -1,  0,  0, -1, 6),   // M1 on Deep went magenta; the reference is cool-neutral
        new Recipe(KODAK, "柯达 Vision3 500T（日光）",       NEUTRAL, -1,  0,  0, 0, K,    3200,  0,  0,  0,  1, 3),
        new Recipe(KODAK, "柯达 Vision 200T（小行星城）",   NEUTRAL, -2, -3,  0, 0, K,    5000,  2,  3,  0,  1, 3),   // yellow-green pastel, teal sky; Retro Photo washed it out
        new Recipe(KODAK, "柯达 Tri-X 400",                     MONO,     0,  2,  2, 0, AUTO, 0,     0,  0,  0,  1, 6),
        new Recipe(KODAK, "柯达 T-Max",                         MONO,     0,  2,  3, 0, AUTO, 0,     0,  0,  0,  0, 6),
        new Recipe(KODAK, "柯达 Tri-X 迫冲1600",           MONO,     0,  0,  0, 0, AUTO, 0,     0,  0,  7,  1, 6),   // HC Mono
        // ---- Cine
        new Recipe(CINE,  "Cinestill 50D（蓝丝绒）",         STD,     -2, -1,  0, 0, K,    5500, -1, -1),   // faded, near-neutral, a touch cool and magenta
        new Recipe(CINE,  "Cinestill 800T",                      NEUTRAL,  0,  0,  0, 0, K,    3200,  0, -1,  0,  1, 6),
        new Recipe(CINE,  "经典电影",                      STD,      0, -1,  0, 0, K,    6000,  2,  0,  0,  0, 3),   // golden and saturated, not muted
        new Recipe(CINE,  "Rec709 视频（略平）",             NEUTRAL, -2, -2,  0, 0, AUTO, 0,     0,  0,  0,  0, 5),
        // ---- Ricoh GR image controls
        new Recipe(RICOH, "理光GR 正片",                    STD,      3,  2,  0, 0, AUTO, 0,     2,  0,  0, -1, 6),
        new Recipe(RICOH, "理光GR 负片",                    NEUTRAL, -2,  1,  0, 0, AUTO, 0,    -1,  1,  0,  1, 6),
        new Recipe(RICOH, "理光GR 漂白",                    NEUTRAL, -6,  3,  0, 0, AUTO, 0,     0,  0),   // sat -8 and below is pure grey on this body
        new Recipe(RICOH, "理光GR 复古",                            STD,     -3, -1,  0, 0, AUTO, 0,     3, -1,  4,  0, 6),
        new Recipe(RICOH, "理光GR 正冲负",                    VIVID,    2,  2,  0, 0, AUTO, 0,    -2,  4),
        new Recipe(RICOH, "理光GR 高对比黑白",                  MONO,     0,  3,  1, 0, AUTO, 0,     0,  0,  7,  0, 6),
        new Recipe(RICOH, "理光GR 硬调单色",                    MONO,     0,  2,  2, 0, AUTO, 0,     0,  0),
        new Recipe(RICOH, "理光GR 柔和单色",                    MONO,     0, -2, -1, 0, AUTO, 0,     0,  0),
        // ---- Leica
        new Recipe(LEICA, "徕卡现代",                  STD,      1,  1,  0, 0, AUTO, 0,     0,  0),
        new Recipe(LEICA, "徕卡经典",                       STD,     -1,  2,  0, 0, AUTO, 0,     1,  0),
        new Recipe(LEICA, "徕卡永恒",                       NEUTRAL, -3, -1,  0, 0, AUTO, 0,     1,  0,  0,  0, 3),
        new Recipe(LEICA, "徕卡黑白",                     MONO,     0,  2,  1, 0, AUTO, 0,     0,  0),
        // ---- Hasselblad
        new Recipe(HASSEL,"哈苏 HNCS 自然",             NEUTRAL, -1, -1,  0, 0, AUTO, 0,     0,  0),
        // ---- Canon / Nikon
        new Recipe(CANIK, "佳能标准",                      STD,      1,  1,  0, 0, AUTO, 0,     1, -1),
        new Recipe(CANIK, "佳能人像",                      PORTRAIT, 0,  0, -1, 0, AUTO, 0,     1, -1),
        new Recipe(CANIK, "佳能忠实",                      NEUTRAL,  0,  0,  0, 0, AUTO, 0,     0,  0),
        new Recipe(CANIK, "尼康平面",                          NEUTRAL, -3, -3, -1, 0, AUTO, 0,     0,  0,  0,  0, 5),
        new Recipe(CANIK, "尼康鲜艳",                         VIVID,    1,  1,  1, 0, AUTO, 0,     0,  0),
        // ---- Panasonic / Olympus
        new Recipe(PANOLY,"松下 L.单色 D",                 MONO,     0,  3,  1, 0, AUTO, 0,     0,  0),
                new Recipe(PANOLY,"松下 L.ClassicNeo",              NEUTRAL, -3, -1,  0, 0, AUTO, 0,     2,  0,  0,  1, 6),
        new Recipe(PANOLY,"奥巴 波普艺术",                     VIVID,    8,  2,  0, 1, AUTO, 0,     0,  0),
        new Recipe(PANOLY,"奥巴 清淡",                LIGHT,   -3, -2,  0, 0, AUTO, 0,     0,  0,  0,  2, 6),
        // ---- Other stocks
        new Recipe(OTHER, "爱克发 Vista 200",                      STD,      2,  1,  0, 0, AUTO, 0,     2, -1,  0,  1, 6),
        new Recipe(OTHER, "爱克发 Ultra 100",                      VIVID,    6,  1,  0, 1, AUTO, 0,     0,  0),
        new Recipe(OTHER, "宝丽来 / Instax",                   STD,     -3, -2,  0, 0, AUTO, 0,     1, -2,  4,  1, 6),
        // ---- Ilford
        new Recipe(ILFORD,"伊尔福 HP5",                          MONO,     0,  1,  0, 0, AUTO, 0,     0,  0,  0,  1, 6),
        new Recipe(ILFORD,"伊尔福 FP4",                          MONO,     0,  1,  1, 0, AUTO, 0,     0,  0),
        new Recipe(ILFORD,"伊尔福 Delta 100",                    MONO,     0,  1,  1, 0, AUTO, 0,     0,  0),
        new Recipe(ILFORD,"伊尔福 Delta 3200",                   MONO,     0,  3, -2, 0, AUTO, 0,     0,  0,  0,  2, 6),
        new Recipe(ILFORD,"伊尔福 Pan F 50",                     MONO,     0,  2,  2, 0, AUTO, 0,     0,  0),
    };

    /** first recipe index of each group */
    public static final int[] GROUP_START = new int[GROUPS.length];
    public static final int[] GROUP_COUNT = new int[GROUPS.length];
    static {
        for (int g = 0; g < GROUPS.length; g++) GROUP_START[g] = -1;
        for (int i = 0; i < ALL.length; i++) {
            int g = ALL[i].group;
            if (GROUP_START[g] < 0) GROUP_START[g] = i;
            GROUP_COUNT[g]++;
        }
    }

    // ---- navigation: every step wraps, dir is +1 / -1
    /** the recipe after / before i over the whole table */
    public static int next(int i, int dir) { return (i + ALL.length + dir) % ALL.length; }
    /** the first recipe of the brand after / before i's */
    public static int nextGroupStart(int i, int dir) { return GROUP_START[(ALL[i].group + GROUPS.length + dir) % GROUPS.length]; }
    /** the recipe after / before i within its brand */
    public static int nextInGroup(int i, int dir) {
        int g = ALL[i].group, start = GROUP_START[g], n = GROUP_COUNT[g];
        return start + ((i - start + n + dir) % n);
    }
}
