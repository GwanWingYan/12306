/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengoofy.index12306.biz.ticketservice.service.handler.ticket.select;

import cn.hutool.core.collection.CollUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 座位选择器
 */
public class SeatSelection {

    public static int[][] select(int numSeats, int[][] seatLayout) {
        int numRows = seatLayout.length;
        int numCols = seatLayout[0].length;
        List<int[]> selectedSeats = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                if (seatLayout[i][j] == 0) {
                    int consecutiveSeats = 0;
                    for (int k = j; k < numCols; k++) {
                        if (seatLayout[i][k] == 0) {
                            consecutiveSeats++;
                            if (consecutiveSeats == numSeats) {
                                for (int l = k - numSeats + 1; l <= k; l++) {
                                    selectedSeats.add(new int[]{i, l});
                                }
                                break;
                            }
                        } else {
                            consecutiveSeats = 0;
                        }
                    }
                    if (!selectedSeats.isEmpty()) {
                        break;
                    }
                }
            }
            if (!selectedSeats.isEmpty()) {
                break;
            }
        }
        if (CollUtil.isEmpty(selectedSeats)) {
            return null;
        }
        int[][] actualSeat = new int[numSeats][2];
        int i = 0;
        for (int[] seat : selectedSeats) {
            int row = seat[0] + 1;
            int col = seat[1] + 1;
            actualSeat[i][0] = row;
            actualSeat[i][1] = col;
            i++;
        }
        return actualSeat;
    }

    public static void main(String[] args) {
        int[][] seatLayout = {
                {1, 1, 1, 1},
                {1, 1, 0, 0},
                {1, 1, 1, 0},
                {0, 0, 0, 0}
        };
        int[][] select = select(5, seatLayout);
        System.out.println("成功预订商务座，座位位置为：");
        assert select != null;
        for (int[] ints : select) {
            System.out.printf("第 %d 排，第 %d 列%n", ints[0], ints[1]);
        }
    }
}
