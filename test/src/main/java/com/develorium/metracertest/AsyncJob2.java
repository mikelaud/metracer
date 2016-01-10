/*
 * Copyright 2015-2016 Michael Kocherov
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

package com.develorium.metracertest;

import com.develorium.metracer.Traced;

public class AsyncJob2 implements Runnable {
	AsyncJob2(final int theDuration) {
		duration_ = theDuration;
	}
	@Traced
	public void run() {
		perform();
	}
	@Traced
	public void perform() {
		try {
			Thread.sleep(duration_);
		} catch (InterruptedException e) {
		}
	}
	private int duration_ = 0;
}
