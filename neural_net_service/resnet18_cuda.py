import torch
import torch.nn as nn
from torchvision import models
from datetime import datetime

from torchvision.datasets.folder import ImageFolder, default_loader
from torchvision import transforms

from torch.utils.data import DataLoader

import numpy as np



class NN():
    IMG_SIZE = 224
    _mean = [0.485, 0.456, 0.406]
    _std = [0.229, 0.224, 0.225]
    four_types = ['crosswalks4', 'traffic lights4', 'fire hydrants4', 'buses4', 'motorcycles4', 'bicycles4']



    def __init__(self):
        self.device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
        self.model_bicycle = self.get_modelR('best_models/bicycle_resnet18_0.9888888888888889.pth', models.resnet18)
        self.model_bus = self.get_modelR('best_models/bus_resnet18_0.9671314741035857.pth', models.resnet18)
        self.model_cars = self.get_modelR('best_models/car_new_resnet18_0.9540322580645161.pth', models.resnet18)
        self.model_crosswalks = self.get_modelR('best_models/crosswalk_resnet18_0.9791666666666666.pth', models.resnet18)
        self.model_fire = self.get_modelR('best_models/fire_hydrant_resnet18_1.0.pth', models.resnet18)
        self.model_lights = self.get_modelR('best_models/traffic_lights_resnet18_unfreezed4_0.9694323144104804.pth',
                                            models.resnet18)
        self.model_store = self.get_modelR('best_models/store_front_resnet18_0.9785714285714285.pth', models.resnet18)
        self.model_croswallks4 = self.get_modelR(
            'best_models/crosswalks4_adamresnet_001_0.9376_0.1867_0.9409_0.9397.pth.tar', models.resnet34)
        self.model_fire4 = self.get_modelR('best_models/fire4_resnet34_0.9584.pth', models.resnet34)
        self.model_motorcycles4 = self.get_modelR(
            'best_models/motorcycles4_adamresnet_001_0.9240_0.1976_0.9017_0.8999.pth.tar', models.resnet34)
        self.model_buses4 = self.get_modelR('best_models/buses4_adamresnet_001_0.9215_0.2437_0.9141_0.9017.pth.tar',
                                            models.resnet34)
        self.model_bicycles4 = self.get_modelR(
            'best_models/bicycles4_adamresnet_001_0.9325_0.2368_0.9341_0.9308.pth.tar', models.resnet34)

        self.models = {'bicycles': self.model_bicycle, 'bus': self.model_bus, 'cars': self.model_cars,
                       'crosswalks': self.model_crosswalks, 'a fire hydrant': self.model_fire,
                       'traffic lights4': self.model_lights, 'store front': self.model_store,
                       'crosswalks4': self.model_croswallks4, 'fire hydrants4': self.model_fire4,
                       'buses4': self.model_buses4, 'motorcycles4': self.model_motorcycles4,
                       'bicycles4': self.model_bicycles4}
        self.thresholds = {'bicycles': 0, 'bus': 0, 'cars': 0,
                           'crosswalks': 0, 'a fire hydrant': 0, 'traffic lights4': 0,
                           'store front': 0, 'crosswalks4': 0, 'fire hydrants4': 0.0,
                           'buses4': 0.0, 'motorcycles4': 0.0, 'bicycles4': 0}


        self.val_trans3 = transforms.Compose([
            transforms.Resize(224),
            transforms.CenterCrop(self.IMG_SIZE),
            transforms.ToTensor(),
            transforms.Normalize(self._mean, self._std),
        ])
        self.val_trans4 = transforms.Compose([
            transforms.Resize(224),
            transforms.ToTensor(),
            transforms.Normalize(self._mean, self._std),
        ])
        self.valid_trans_store = transforms.Compose([
            transforms.Resize(256),
            transforms.RandomCrop(224),
            transforms.ToTensor(),
            transforms.Normalize(self._mean, self._std),
        ])

        print('---------Load neural network model done----------')


    def get_modelR(self, model_name, torch_model):

        print(f"Load model {model_name.split('/')[1]}")
        model = torch_model(pretrained=False)
        model.fc = nn.Linear(512, 2)
        if self.device == "cuda:0":
            model.load_state_dict(torch.load(model_name))
            model.to(self.device)
        else:
            model.load_state_dict(torch.load(model_name, map_location='cpu'))
        model.eval()
        return model

    def freeze_all(self, model_params):
        for param in model_params:
            param.requires_grad = False



    def predict(self, path, type_of_image, indices):
        start_time = datetime.now()
        try:
            if type_of_image in self.four_types:
                loader = self.loader(path, self.val_trans4)
            elif type_of_image == 'store front':
                loader = self.loader(path, self.valid_trans_store)
            else:
                loader = self.loader(path, self.val_trans3)
        except RuntimeError:
            return []

        ind = np.array(indices)
        thd = self.thresholds[type_of_image]
        mod = self.models[type_of_image]
        print(type_of_image)
        with torch.no_grad():
            try:
                for X, y in loader:
                    if self.device == "cuda:0":
                        X, y = X.to(self.device), y.to(self.device)
                    y_ = mod(X).detach().cpu().numpy()
                    if type_of_image in self.four_types:
                        ans = (y_ > thd)[:, 1].astype(int).reshape(-1)
                    elif type_of_image != 'store front':
                        ans = (y_ > thd)[:, 0].astype(int).reshape(-1)
                    else:
                        ans = (y_ > thd)[:, 1].astype(int).reshape(-1)
                    print(y_)

                end_time = datetime.now()
                print(ans)
                execution_time = end_time - start_time
                print('Duration: {}'.format(execution_time))
            except OSError as ex:
                return [choice(indices)]        
        return list(ind[np.where(ans == 1)])



    def loader(self, path, transf):
        dataset = ImageFolder(path, transform=transf)
        dataloader = DataLoader(
            dataset,
            batch_size=16,
            shuffle=False,
            num_workers=0,
        )
        return dataloader










