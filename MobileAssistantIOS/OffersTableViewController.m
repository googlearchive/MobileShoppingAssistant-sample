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

#import "ExtraPropertyUITableViewCell.h"
#import "GTLShoppingassistant.h"
#import "GTMHTTPFetcher.h"
#import "OffersTableViewController.h"
#import "ViewHelper.h"

@implementation OffersTableViewController

@synthesize offers = _offers;
@synthesize recommendations = _recommendations;

NSString *const kOfferTableCellID = @"OfferPrototypeCell";

enum { kOffers = 0, kRecommendations, kNumberOfSections };

#pragma mark - Customized setters and getters

- (void)setOffers:(NSArray *)array {
  if (array != _offers) {
    _offers = [array copy];

    if (self.tableView.window) {
      [self.tableView reloadData];

      // Remove visual spinner if both recommendation and offers are set
      UINavigationItem *navigationItem = self.navigationItem;
      if (self.recommendations && navigationItem.rightBarButtonItem) {
        navigationItem.rightBarButtonItem = nil;
      }
    }
  }
}

- (void)setRecommendations:(NSArray *)array {
  if (array != _recommendations) {
    _recommendations = [array copy];

    if (self.tableView.window) {
      [self.tableView reloadData];

      // Remove visual spinner if both recommendation and offers are set
      UINavigationItem *navigationItem = self.navigationItem;
      if (self.offers && navigationItem.rightBarButtonItem) {
        navigationItem.rightBarButtonItem = nil;
      }
    }
  }
}

#pragma mark - UITableVew data source for offers/recommendations

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
  return kNumberOfSections;
}

- (NSInteger)tableView:(UITableView *)tableView
    numberOfRowsInSection:(NSInteger)section {
  if (section == kOffers) {
    return [self.offers count];
  } else if (section == kRecommendations) {
    return [self.recommendations count];
  }
  return 0;
}

- (NSString *)tableView:(UITableView *)tableView
    titleForHeaderInSection:(NSInteger)section {
  if (section == kOffers) {
    return @"Offers";
  } else if (section == kRecommendations) {
    return @"Recommendations";
  }

  return nil;
}

- (void)provideBlankCellImage:(UITableView *)tableView
                    indexPath:(NSIndexPath *)indexPath
                    tableCell:(UITableViewCell *)cell {
  CGSize imageSize =
      CGSizeMake(kViewHelperDefaultCellImageWidth,
                 [self tableView:tableView heightForRowAtIndexPath:indexPath]);
  UIGraphicsBeginImageContextWithOptions(imageSize, NO, 0.0);
  cell.imageView.image = UIGraphicsGetImageFromCurrentImageContext();
  UIGraphicsEndImageContext();
}

- (UITableViewCell *)tableView:(UITableView *)tableView
         cellForRowAtIndexPath:(NSIndexPath *)indexPath {
  ExtraPropertyUITableViewCell *cell =
      [tableView dequeueReusableCellWithIdentifier:kOfferTableCellID
                                      forIndexPath:indexPath];
  if (!cell) {
    cell = [[ExtraPropertyUITableViewCell alloc] initWithStyle:UITableViewCellStyleValue2
                                               reuseIdentifier:kOfferTableCellID];
  }

  if (indexPath.section == kOffers) {
    // Offer section
    GTLShoppingAssistantOffer *offer =
        [self.offers objectAtIndex:indexPath.row];

    cell.textLabel.text = offer.title;
    cell.textLabel.textColor =
        [UIColor colorWithRed:1.0 green:0.51 blue:0 alpha:1.0];
    cell.detailTextLabel.numberOfLines = 0;
    cell.detailTextLabel.lineBreakMode = NSLineBreakByWordWrapping;
    cell.detailTextLabel.text = offer.descriptionProperty;

    [self provideBlankCellImage:tableView indexPath:indexPath tableCell:cell];
    cell.indexPath = indexPath;
    [self configureCellImage:tableView
                   tableCell:cell
              imageURLString:offer.imageUrl
                   indexPath:indexPath];

  } else if (indexPath.section == kRecommendations) {
    // Recommendation section
    GTLShoppingAssistantRecommendation *recommendation =
        [self.recommendations objectAtIndex:indexPath.row];

    cell.textLabel.text = recommendation.title;
    cell.textLabel.textColor =
        [UIColor colorWithRed:0.6 green:0.22 blue:0 alpha:1.0];
    cell.detailTextLabel.numberOfLines = 0;
    cell.detailTextLabel.lineBreakMode = NSLineBreakByWordWrapping;
    cell.detailTextLabel.text = recommendation.descriptionProperty;

    [self provideBlankCellImage:tableView indexPath:indexPath tableCell:cell];
    cell.indexPath = indexPath;
    [self configureCellImage:tableView
                   tableCell:cell
              imageURLString:recommendation.imageUrl
                   indexPath:indexPath];
  }

  return cell;
}

- (CGFloat)tableView:(UITableView *)tableView
    heightForRowAtIndexPath:(NSIndexPath *)indexPath {
  if (indexPath.section == kOffers) {
    // Offer section
    GTLShoppingAssistantOffer *offer =
        [self.offers objectAtIndex:indexPath.row];
    NSInteger length = (NSInteger)offer.descriptionProperty.length;
    return [ViewHelper heightForCellDetailWithLineCount:length];

  } else if (indexPath.section == kRecommendations) {
    // Recommendation section
    GTLShoppingAssistantRecommendation *recommendation =
        [self.recommendations objectAtIndex:indexPath.row];
    int length = (int)recommendation.descriptionProperty.length;
    return [ViewHelper heightForCellDetailWithLineCount:length];
  }

  return kViewHelperDefaultCellHeight;
}

- (void)configureCellImage:(UITableView *)tableView
                 tableCell:(ExtraPropertyUITableViewCell *)cell
            imageURLString:(NSString *)imageURLString
                 indexPath:(NSIndexPath *)indexPath {
  GTMHTTPFetcher *imageFetcher = [GTMHTTPFetcher fetcherWithURLString:imageURLString];
  imageFetcher.retryEnabled = YES;
  imageFetcher.comment = @"Cell image";

  [imageFetcher beginFetchWithCompletionHandler:^(NSData *data, NSError *error) {
    UIImage *fetchedImage = nil;

    if (error) {
      // It's not wise to show error message to users for each image
      // fetching failure, but we should log for debugging.
      NSLog(@"%@", error);
    } else {
      if ([data length] > 0) {
        fetchedImage = [[UIImage alloc] initWithData:data];
      }
    }

    // Attempt to display the image only when the fetched image is valid
    if (fetchedImage) {
      // Display the image only when the recyled cell is corresponding to
      // the matching indexPath
      if ([indexPath isEqual:cell.indexPath]) {
        cell.imageView.contentMode = UIViewContentModeScaleAspectFit;
        cell.imageView.clipsToBounds = YES;
        // Set the cell to display the image
        cell.imageView.image = fetchedImage;
      }
    }
  }];
}

@end