package li.klass.fhem.domain;

import li.klass.fhem.appwidget.annotation.ResourceIdMapper;
import li.klass.fhem.domain.core.DeviceFunctionality;
import li.klass.fhem.domain.core.DimmableDevice;
import li.klass.fhem.domain.genericview.OverviewViewSettings;
import li.klass.fhem.domain.genericview.ShowField;
import li.klass.fhem.util.ColorUtil;
import li.klass.fhem.util.NumberSystemUtil;
import li.klass.fhem.util.ValueDescriptionUtil;
import li.klass.fhem.util.ValueExtractUtil;

@SuppressWarnings("unused")
@OverviewViewSettings(showState = true)
public class HUEDevice extends DimmableDevice<HUEDevice> {

    private double[] xy;
    private int rgb;

    public enum SubType {
        COLORDIMMER, DIMMER, SWITCH
    }

    private SubType subType;

    @ShowField(description = ResourceIdMapper.model, showAfter = "definition")
    private String model;

    private Integer brightness;
    private Integer saturation;
    private int level;

    public void readSUBTYPE(String value) {
        try {
            subType = SubType.valueOf(value.toUpperCase());
        } catch (Exception e) {
            subType = null;
        }
    }

    public void readMODEL(String value) {
        model = value;
    }

    public void readBRI(String value) {
        brightness = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readHUE(String value) {
        Integer hue = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readSAT(String value) {
        saturation = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readLEVEL(String value) {
        level = ValueExtractUtil.extractLeadingInt(value);
    }

    public void readSTATE(String value) {
        setState(value);
    }

    public void readXY(String value) {
        String[] parts = value.split(",");
        xy = new double[]{Double.valueOf(parts[0]), Double.valueOf(parts[1])};
    }

    public void readRGB(String value) {
        rgb = NumberSystemUtil.hexToDecimal(value);

        ColorUtil.XYColor xyColor = ColorUtil.rgbToXY(rgb);
        brightness = xyColor.brightness;
        xy = xyColor.xy;
    }

    @Override
    public void setState(String state) {
        if (state.equals("off")) level = 0;
        if (state.equals("on")) level = getDimUpperBound();

        if (state.startsWith("pct")) {
            level = getPositionForDimState(state);
            super.setState(ValueDescriptionUtil.appendPercent(level));
        } else {
            super.setState(state);
        }
    }

    @Override
    public int getDimUpperBound() {
        return 100;
    }

    @Override
    public String getDimStateForPosition(int position) {
        return "pct " + position;
    }

    @Override
    public int getPositionForDimState(String dimState) {
        String value = dimState.replace("pct ", "").trim();
        if (value.equals("on")) return getDimUpperBound();
        if (value.equals("off")) return 0;

        return ValueExtractUtil.extractLeadingInt(value);
    }

    @Override
    public void afterDeviceXMLRead() {
        super.afterDeviceXMLRead();

        if (xy != null) {
            this.rgb = ColorUtil.xyToRgb(xy, brightness);
        }
    }

    @Override
    public boolean supportsDim() {
        return subType == SubType.COLORDIMMER || subType == SubType.DIMMER;
    }

    @Override
    public boolean supportsToggle() {
        return supportsDim() || subType == SubType.SWITCH;
    }

    @Override
    public boolean isSupported() {
        return super.isSupported() && subType != null;
    }

    public SubType getSubType() {
        return subType;
    }

    public int getBrightness() {
        return brightness;
    }

    @ShowField(description = ResourceIdMapper.brightness)
    public String getBrightnessDesc() {
        return brightness + "";
    }

    public String getModel() {
        return model;
    }

    public void setRgb(int rgb) {
        this.rgb = rgb;
    }

    public int getRgb() {
        return rgb;
    }

    public double[] getXy() {
        return xy;
    }

    @ShowField(description = ResourceIdMapper.color)
    public String getRgbDesc() {
        return ColorUtil.toHexString(rgb, 6);
    }

    public int getSaturation() {
        return saturation;
    }

    @ShowField(description = ResourceIdMapper.saturation)
    public String getSaturationDesc() {
        return saturation + "";
    }

    @Override
    public String getDimStateFieldValue() {
        return level + "";
    }

    @Override
    public boolean acceptXmlKey(String key) {
        if ("name".equals(key)) return false;
        return super.acceptXmlKey(key);
    }

    @Override
    public DeviceFunctionality getDeviceFunctionality() {
        return DeviceFunctionality.functionalityForDimmable(this);
    }
}
