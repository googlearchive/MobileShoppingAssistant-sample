/*
 * Copyright 2015 Google Inc. All Rights Reserved.
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

// Helper class to assist any computation or rendering for any views in the
// application.
@interface ViewHelper : NSObject

// Public constants for UITableViewCell.
extern int const kViewHelperDefaultCellHeight;
extern int const kViewHelperDefaultCellImageWidth;
extern int const kViewHelperDetailTitleHeightPerLine;

// Display a top right corner spinner at the top navigation tool bar for the
// input viewController to indicate the controller is busy with fetching data
// from server.  UIViewController caller needs to handle the removal of the
// spinner by setting navigationItem rightBarButtonItem = nil.
+ (void)showToolbarSpinnerForViewController:(UIViewController *)viewController;

// Display a top left corner "Sign in" button at the top navigation tool bar
// for the input viewController.
+ (void)showSigninToolBarButtonForViewController:
        (UIViewController *)viewController;

// Display a top left corner "Sign out" button at the top navigation tool bar
// for the input viewController.
+ (void)showSignoutToolBarButtonForViewController:
        (UIViewController *)viewController;

// Display a standard alert style popup for current view.
+ (void)showPopup:(NSString *)title
          message:(NSString *)message
           button:(NSString *)label;

// Calculate the UITableViewCell height based on text length provided.
+ (CGFloat)heightForCellDetailWithLineCount:(NSInteger)textLength;
@end
