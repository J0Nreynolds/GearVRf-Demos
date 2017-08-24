/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.painting;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.view.GVRFrameLayout;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class PaintingActivity extends GVRActivity {
    private static final String TAG = PaintingActivity.class.getSimpleName();
    private PaintingMain main;

    private GVRFrameLayout frameLayout;

    private SeekBar sizeSeekBar, redSeekBar, greenSeekBar, blueSeekBar;
    private Button clearButton, finishButton;
    private TextView currentColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        frameLayout = new GVRFrameLayout(this);
        frameLayout.setBackgroundColor(Color.WHITE);
        View.inflate(this, R.layout.paint_menu, frameLayout);
        frameLayout.setLayoutParams(new GVRFrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2260));

        sizeSeekBar = (SeekBar) frameLayout.findViewById(R.id.sizeSeekBar);
        redSeekBar = (SeekBar) frameLayout.findViewById(R.id.redSeekBar);
        greenSeekBar = (SeekBar) frameLayout.findViewById(R.id.greenSeekBar);
        blueSeekBar = (SeekBar) frameLayout.findViewById(R.id.blueSeekBar);

        clearButton = (Button) frameLayout.findViewById(R.id.clearButton);
        currentColor = (TextView) frameLayout.findViewById(R.id.currentColor);

        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                main.setBrushSize(i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        redSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentColor.setBackgroundColor(Color.rgb(i, greenSeekBar.getProgress(), blueSeekBar.getProgress()));
                main.setColor(i, greenSeekBar.getProgress(), blueSeekBar.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        greenSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentColor.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), i, blueSeekBar.getProgress()));
                main.setColor(redSeekBar.getProgress(), i, blueSeekBar.getProgress());
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        blueSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentColor.setBackgroundColor(Color.rgb(redSeekBar.getProgress(), greenSeekBar.getProgress(), i));
                main.setColor(redSeekBar.getProgress(), greenSeekBar.getProgress(), i);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                main.clearCanvas();
            }
        });

        main = new PaintingMain(this, frameLayout);
        setMain(main);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent){
        if(motionEvent.getToolType(0) == MotionEvent.TOOL_TYPE_UNKNOWN)
            return false;
        else
            return super.dispatchTouchEvent(motionEvent);
    }
}
